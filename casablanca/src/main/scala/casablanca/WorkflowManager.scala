package casablanca

import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.TaskHandler
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.TaskManager
import casablanca.task.TaskHandlerFactory
import java.util.Date
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueue
import casablanca.queues.StatusQueueWorker

/**
 *
 */
trait WorkflowManager {

  def start
  def stop
}

class WorkflowManagerImpl(tm: TaskManager,
  statusQManager: StatusQueueManager,
  statusHandlerFactory: TaskHandlerFactoryFactory,
  scheduler: Scheduler) extends WorkflowManager {

  def stop {
    // todo add other stops for threads
    tm.close
  }

  def start {

    val statusQueues = statusQManager.statusQueues
    statusQueues.map(q => q.init)
    val workerQueue = new java.util.concurrent.ArrayBlockingQueue[StatusQueue](statusQueues.size)
    statusQueues.foreach { e => workerQueue.put(e) }
    for (i <- 0 to 5) {
      new StatusQueueWorker(workerQueue).start
    }
    scheduler.start
  }

}