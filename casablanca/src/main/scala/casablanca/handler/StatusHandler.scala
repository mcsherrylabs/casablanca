package casablanca.handler

import casablanca.task.Task


trait Schedule 

object NoSchedule extends Schedule

trait StatusHandler {
	def handle(task: Task): Int
	def getSchedule: Schedule
	def reTry(task: Task): Int
}


trait StatusHandlerFactory {

  def getSupportedStatuses: List[Int]  
  def getHandler(status:Int) : Option[StatusHandler]
}
