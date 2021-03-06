package sss.casablanca.queues

import java.util.concurrent.TimeUnit

import _root_.sss.ancillary.Logging
import sss.casablanca.task.{ RelativeScheduledStatusUpdate, StatusConfig, SystemFailure, Task, TaskFatalError, TaskHandlerContext }

class StatusQueue(taskContext: TaskHandlerContext,
    statusConfig: StatusConfig,
    status: Int,
    val taskType: String,
    statusQueueManager: StatusQueueManager) extends Logging {

  private val statusHandler = statusQueueManager.getHandler(taskType, status).getOrElse(throw new Error(s"No handler exists for status ${taskType}:${status}"))
  private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](statusConfig.queueSize)

  def init {
    statusQueueManager.findTasks(taskType, status).foreach(t => queue.add(t))
  }

  def push(t: Task): Boolean = {
    // check status?
    queue.offer(t, statusConfig.offerTimeoutMs, TimeUnit.MILLISECONDS)
  }

  def poll: Task = {
    val t = queue.poll(statusConfig.pollTimeoutMs, TimeUnit.MILLISECONDS)
    if (t != null) statusQueueManager.attemptTask(t)
    else t
  }

  private var loopCount = 0

  def run(t: Task) {

    log.debug(s"StatusQueueWorker for ${status} attempts to handle task ${t.taskType}:${t.id}")
    statusQueueManager.tx {
      try {
        if (t.attemptCount > 1) {
          if (t.attemptCount <= statusConfig.maxRetryCount) {
            val handlerResult = statusHandler.reTry(taskContext, t)
            statusQueueManager.pushTask(t, handlerResult)
          } else log.warn(s"Giving up on task ${t}, max try count exceeded (${statusConfig.maxRetryCount})")
        } else {
          val handlerResult = statusHandler.handle(taskContext, t)
          statusQueueManager.pushTask(t, handlerResult)
        }

      } catch {

        case tf: TaskFatalError => {
          log.error(s"FATAL problem handling task, abandoning ${t}", tf)
          statusQueueManager.pushTask(t, SystemFailure())
        }
        case ex: Exception => {
          log.warn(s"Exception handling task, retry in ${statusConfig.retryDelayMinutes} minutes", ex)
          statusQueueManager.pushTask(t, RelativeScheduledStatusUpdate(t.status, statusConfig.retryDelayMinutes))
        }
      }
    }

  }
}

