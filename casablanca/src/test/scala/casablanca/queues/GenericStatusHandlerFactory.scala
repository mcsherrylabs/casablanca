package casablanca.queues

import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.TaskHandlerFactory
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import casablanca.task.TaskManager
import casablanca.task.TaskHandlerContext



class GenericStatusHandler(val status: Int) extends TaskHandler {
    
	def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
	  println(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")
	  Thread.sleep(1000)
	  if(task.status == 4 && task.attemptCount < 3) {
	    println("Throwing!")
	    throw new RuntimeException("Whas goin on?")
	  }
	  StatusUpdate(status + 1)
	}
	
	override def reTry(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
	  println(s"RETRY ${task.id} status ${task.status}, count ${task.attemptCount}  ")
	  handle(taskContext, task)
	}
}

class GenericStatusHandlerFactory(val tm: TaskManager) extends TaskHandlerFactory {

  def getTaskType : String = "genericTask"
  
  def getSupportedStatuses: Set[Int] = (0 to 9).toSet
  
  def getHandler[T >: TaskHandler](status:Int) : Option[TaskHandler] = {    
    if(status < 10) Some(new GenericStatusHandler(status))
    else None
  } 
}


