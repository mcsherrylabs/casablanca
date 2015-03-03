package casablanca.task

import java.util.Date
import java.util.LinkedHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import java.util.concurrent.TimeUnit



sealed case class TaskStatus(value: Int)

trait SystemTaskStatuses {      
  val systemFinished = TaskStatus(0)
  val systemFailed = TaskStatus(1)     
}

trait TaskStatuses extends SystemTaskStatuses {
  val taskFinished = TaskStatus(100)
  val taskFailed = TaskStatus(101)
  val taskStarted = TaskStatus(102)         
}

//object TaskStatus extends SystemTaskStatus {
//      type TaskStatus = Value
//      val taskFinished, taskStarted, taskFailed = Value  
//}

/*trait TaskStatus {
  val systemFinished = 0
  val systemFailed = 1
  val systemSuccess = 2
  val systemStarted = 10

  val taskFinished = 100
  val taskStarted = 101
}*/

case class RemoteTask(node: String, taskType: String)

trait HandlerUpdate

trait TaskHandler extends TaskStatus {
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate
  def reTry(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = handle(taskHandlerContext, task)
}

trait TaskHandlerFactory extends TaskStatus {

  def getTaskType: String
  def getSupportedStatuses: Set[TaskStatus]
  def getHandler[T >: TaskHandler](status: TaskStatus): Option[T]
  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: String) 
  def consume(taskContext: TaskHandlerContext, task: Task, event: String): Option[StatusUpdate] 
    
}

object RelativeScheduledStatusUpdate {

  def apply(nextStatus: Int, minutesInFuture: Int, newStringPayload: Option[String] = None): ScheduledStatusUpdate = {
    ScheduledStatusUpdate(nextStatus, createFutureDate(minutesInFuture), newStringPayload)
  }

  private def createFutureDate(minutesInFuture: Int): Date = {
    val now = new Date()
    new Date(now.getTime + (minutesInFuture * 1000 * 60))
  }
}

case class StatusUpdate(nextStatus: Int, newStringPayload: Option[String] = None, attemptCount: Int = 0) extends HandlerUpdate
case class ScheduledStatusUpdate(nextStatus: Int, scheduleAfter: Date, newStringPayload: Option[String] = None) extends HandlerUpdate

