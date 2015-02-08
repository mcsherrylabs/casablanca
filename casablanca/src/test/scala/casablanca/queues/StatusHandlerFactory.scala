package casablanca.queues

import casablanca.task.Task
import casablanca.handler.StatusHandler
import casablanca.handler.StatusHandlerFactory
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate



class GenericStatusHandler(val status: Int) extends StatusHandler {
    
	def handle(task: Task): HandlerUpdate = {
	  println(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")
	  Thread.sleep(1000)
	  if(task.status == 4 && task.attemptCount < 3) {
	    println("Throwing!")
	    throw new RuntimeException("Whas goin on?")
	  }
	  StatusUpdate(status + 1)
	}
	
	def reTry(task: Task): HandlerUpdate = {
	  println(s"RETRY ${task.id} status ${task.status}, count ${task.attemptCount}  ")
	  handle(task)
	}
}

class GenericStatusHandlerFactory extends StatusHandlerFactory {

  def getSupportedStatuses: List[Int] = (0 to 9).toList
  
  def getHandler(status:Int) : Option[StatusHandler] = {    
    if(status < 10) Some(new GenericStatusHandler(status))
    else None
  } 
}


