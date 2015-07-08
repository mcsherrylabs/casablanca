package casablanca.task

import java.util.Date
import java.util.LinkedHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import java.util.concurrent.TimeUnit
import _root_.sss.ancillary.Logging

sealed case class TaskStatus(value: Int)

trait SystemTaskStatuses {
  val systemFinished = TaskStatus(0)
  val systemFailed = TaskStatus(1)
}

trait TaskStatuses extends SystemTaskStatuses {
  val taskFinished = TaskStatus(100)
  val taskFailed = TaskStatus(101)
  val taskStarted = TaskStatus(102)
  val awaitingEvent = TaskStatus(103)
}

case class HandlerUpdate(
  nextStatus: Option[Int] = None,
  newStringPayload: Option[String] = None,
  scheduleAfter: Option[Option[Date]] = None) extends TaskStatuses

trait TaskHandler extends TaskStatuses with Logging {
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate
  def reTry(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    log.info(s"Default handler retrying task ${task}")
    handle(taskHandlerContext, task)
  }
}

case class StatusConfig(
  queueSize: Int = 25,
  offerTimeoutMs: Int = 10000,
  pollTimeoutMs: Int = 100,
  maxRetryCount: Int = 5,
  retryDelayMinutes: Int = 0)

trait TaskHandlerFactory extends TaskStatuses {

  def getStatusConfig(status: Int): StatusConfig
  def getTaskType: String
  def getSupportedStatuses: Set[TaskStatus]
  def getHandler[T >: TaskHandler](status: TaskStatus): Option[T]
  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: TaskEvent)
  def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate]

}

object RelativeScheduledStatusUpdate {

  def apply(nextStatus: Int, minutesInFuture: Int, newStringPayload: Option[String] = None): HandlerUpdate = {
    ScheduledStatusUpdate(nextStatus, createFutureDate(minutesInFuture), newStringPayload)
  }

  private def createFutureDate(minutesInFuture: Int): Date = {
    val now = new Date()
    new Date(now.getTime + (minutesInFuture * 1000 * 60))
  }
}

object HandlerUpdate extends TaskStatuses {
  val systemSuccess = HandlerUpdate(Some(systemFinished.value))
  val success = HandlerUpdate(Some(taskFinished.value))
  val awaitEvent = HandlerUpdate(Some(awaitingEvent.value))
  val failure = HandlerUpdate(Some(taskFailed.value))
}

object ScheduledStatusUpdate {
  def apply(nextStatus: Int, scheduleAfter: Date, newStringPayload: Option[String] = None) = HandlerUpdate(Some(nextStatus), newStringPayload, Some(Some(scheduleAfter)))
}

object SystemSuccess extends TaskStatuses {
  def apply(): HandlerUpdate = {
    HandlerUpdate(Some(systemFinished.value))
  }
}

object SystemFailure extends TaskStatuses {
  def apply(): HandlerUpdate = {
    HandlerUpdate(Some(systemFailed.value))
  }
}

object Success extends TaskStatuses {
  def apply(): HandlerUpdate = {
    HandlerUpdate(Some(taskFinished.value))
  }
}

object AwaitEvent extends TaskStatuses {
  def apply(): HandlerUpdate = {
    HandlerUpdate(Some(awaitingEvent.value))
  }
}

object StatusUpdate {
  def apply(nextStatus: Int, newStringPayload: Option[String] = None) = HandlerUpdate(Some(nextStatus), newStringPayload)

}

