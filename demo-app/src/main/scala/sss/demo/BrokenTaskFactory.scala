package sss.demo

import sss.casablanca.task.{ BaseTaskHandlerFactory, HandlerUpdate, Success, Task, TaskHandler, TaskHandlerContext, TaskStatus }

trait BrokenStatuses {
  val brokenTask = "brokenTask"

}

object BreakTaskHandler extends TaskHandler with BrokenStatuses {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    task.strPayload match {
      case "BREAK" => {
        log.info(s"BREAKING!")
        throw new RuntimeException("WE ARE ALWAYS GOING TO FAIL...!")
      }
      case x => {
        log.info(s"NOT BREAKING")
        Success()
      }

    }

  }
}

object BrokenTaskFactory extends BaseTaskHandlerFactory with BrokenStatuses {

  def getTaskType: String = brokenTask

  override def getSupportedStatuses: Set[TaskStatus] = super.getSupportedStatuses ++ Set(taskStarted)

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(BreakTaskHandler)
    case _ => super.getHandler(status)
  }

}
