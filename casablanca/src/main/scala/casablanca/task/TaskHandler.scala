package casablanca.task

import java.util.Date

trait HandlerUpdate 

trait TaskHandler {
	def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate	
	def reTry(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = handle(taskHandlerContext, task)
}


trait TaskHandlerContext {
  def createTask(taskType: String, status: Int, strPayload: String = "", intPayload: Int = 0, scheduleTime: Option[Date] = None): Task 
  def startTask(taskType: String, status: Int, strPayload: String = "", intPayload: Int = 0, scheduleTime: Option[Date] = None): Task 
}

trait TaskHandlerFactory {
  
  //def init(taskHandlerContext: TaskHandlerContext, status: Int = 0, strPayload: String = "", intPayload: Int = 0, scheduleTime: Option[Date] = None) : Option[Task] 
  
  def getTaskType: String
  def getSupportedStatuses: Set[Int]  
  def getHandler[T >: TaskHandler](status:Int) : Option[T]
}

trait TaskHandlerFactoryFactory {
  def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T]
  def getSupportedFactories: List[TaskHandlerFactory]  
  def getHandler(taskType: String, status:Int) : Option[TaskHandler]
}

object TaskHandlerFactoryFactory {
  def apply(factories: TaskHandlerFactory*): TaskHandlerFactoryFactory = new TaskHandlerFactoryFactory {
  
	  def getSupportedFactories: List[TaskHandlerFactory] = factories.toList
	  
	  def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T] = {
	    factories.find(tf => tf.getTaskType == taskType).map(_.asInstanceOf[T])
	  }
	  
      def getHandler(taskType: String, status:Int): Option[TaskHandler] = {
	    val f = getSupportedFactories.find(tf => tf.getTaskType == taskType)
	    f.flatMap( _.getHandler(status))	    
	  } 
  }
    
   
}


object RelativeScheduledStatusUpdate {
    
  def apply(nextStatus: Int, minutesInFuture: Int, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None ): ScheduledStatusUpdate = {
    ScheduledStatusUpdate(nextStatus, createFutureDate(minutesInFuture), newStringPayload, newIntValue)
  } 
  
  private def createFutureDate(minutesInFuture: Int): Date = {
    val now = new Date()
    new Date(now.getTime + (minutesInFuture * 1000 * 60))
  }
}

case class StatusUpdate(nextStatus: Int, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None,  attemptCount: Int = 0) extends HandlerUpdate 
case class ScheduledStatusUpdate(nextStatus: Int, scheduleAfter: Date, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None ) extends HandlerUpdate


