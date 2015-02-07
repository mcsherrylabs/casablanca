package skyhook

import casablanca.task.Task

trait StatusHandler {
	def handle(task: Task): Int
	def reTry(task: Task): Int
}


class GenericStatusHandler(val status: Int) extends StatusHandler {
	def handle(task: Task): Int = {
	  println(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")
	  Thread.sleep(1000)
	  if(task.status == 4 && task.attemptCount < 3) {
	    println("T3hrowing!")
	    throw new RuntimeException("Whas goin on?")
	  }
	  status + 1
	}
	
	def reTry(task: Task): Int = {
	  println(s"RETRY ${task.id} status ${task.status}, count ${task.attemptCount}  ")
	  handle(task)
	}
}

class StatusHandlerFactory {

  def getSupportedStatuses: List[Int] = (0 to 10).toList
  
  def getHandler(status:Int) : Option[StatusHandler] = {    
    if(status < 10) Some(new GenericStatusHandler(status))
    else None
  } 
}