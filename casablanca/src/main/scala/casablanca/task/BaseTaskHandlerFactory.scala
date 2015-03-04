package casablanca.task

import java.util.LinkedHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import casablanca.util.Lockable
import casablanca.util.Logging
import casablanca.webservice.remotetasks.TaskDoneHandler

/*object TaskFinishedHandler extends TaskHandler with Logging {
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    log.info(s"FINSHED(SUCCESS)${task}")
    StatusUpdate(systemFinished.value)
  }
}

object TaskFailedHandler extends TaskHandler with Logging {
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    log.info(s"FINSHED(FAILED) ${task}")
    StatusUpdate(systemFinished.value)
  }
}*/

trait BaseTaskHandlerFactory extends TaskHandlerFactory with Lockable[String] with Logging {

  override def getSupportedStatuses: Set[TaskStatus] = Set(taskFinished, taskFailed)

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = {
    status match {
      case `taskFinished` => Some(TaskDoneHandler)
      case `taskFailed` => Some(TaskDoneHandler)
      case x => {
        log.warn(s"No handler found for status ${x} in Base Factory ")
        None
      }
    }
  }

  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: TaskEvent) {

    log.debug(s"Consuming event from ${ev.origin}")
    log.debug(s"Consuming event payload ${ev.eventPayload}")
    doLocked[Option[HandlerUpdate]](task.id, () => consume(taskContext, task, ev)) map { up => taskContext.pushTask(task, up) }
  }

  def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate] = {
    throw new UnsupportedOperationException("One must override 'consume' in order to consume events.")
  }

}

