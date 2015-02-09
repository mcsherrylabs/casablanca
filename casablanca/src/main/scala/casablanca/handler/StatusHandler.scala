package casablanca.handler

import casablanca.task.Task
import java.util.Date

trait HandlerUpdate

trait StatusHandler {
	def handle(task: Task): HandlerUpdate	
	def reTry(task: Task): HandlerUpdate = handle(task)
}


trait StatusHandlerFactory {
  def getTaskType: String
  def getSupportedStatuses: List[Int]  
  def getHandler[T >: Task](status:Int) : Option[StatusHandler]
}



object RelativeScheduledStatusUpdate {
    
  def apply(nextStatus: Int, minutesInFuture: Int): ScheduledStatusUpdate = {
    ScheduledStatusUpdate(nextStatus, createFutureDate(minutesInFuture))
  } 
  
  private def createFutureDate(minutesInFuture: Int): Date = {
    val now = new Date()
    new Date(now.getTime + (minutesInFuture * 1000 * 60))
  }
}

case class StatusUpdate(nextStatus: Int, attemptCount: Int = 0) extends HandlerUpdate 
case class ScheduledStatusUpdate(nextStatus: Int, scheduleAfter: Date) extends HandlerUpdate

