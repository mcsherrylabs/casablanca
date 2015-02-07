package casablanca.handler

import casablanca.task.Task
import java.util.Date
import casablanca.task.StatusUpdate

trait StatusHandler {
	def handle(task: Task): StatusUpdate	
	def reTry(task: Task): StatusUpdate
}


trait StatusHandlerFactory {

  def getSupportedStatuses: List[Int]  
  def getHandler(status:Int) : Option[StatusHandler]
}
