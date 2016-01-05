package sss.demo

import sss.db.{ Db, Table }
import sss.casablanca.task.{ BaseTaskHandlerFactory, HandlerUpdate, StatusUpdate, Success, Task, TaskHandler, TaskHandlerContext, TaskStatus }
import sss.casablanca.util.ProgrammingError

import scala.language.reflectiveCalls

trait LoadTestStatuses {
  val loadTestTask = "loadTestTask"
  val writeDatabase = TaskStatus(1000)

}

class SetupDatabase(table: Table, isBroken: Boolean) extends TaskHandler with LoadTestStatuses {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    table.insert(isBroken, 0, task.id)
    StatusUpdate(writeDatabase.value)
  }

}

class WriteDatabase(table: Table) extends TaskHandler with LoadTestStatuses {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    table.getRow(s"tid = '${task.id}'") match {
      case None => throw new ProgrammingError(s"row does not exist ${task}")
      case Some(row) => {
        log.info(s"Row is ${row}")
        val isBroken = row[Boolean]("broken")
        if (isBroken) throw new RuntimeException(s"WE'RE BROKEN (${task.attemptCount})")
        table.update("task_count = (task_count + 1)", s" tid = '${task.id}'")
        Success()
      }
    }
  }
}

object LoadTestTaskFactory extends BaseTaskHandlerFactory with LoadTestStatuses {

  def getTaskType: String = loadTestTask

  lazy val table = Db("loadTestTaskDb").table("casablanca_test")

  override def getSupportedStatuses: Set[TaskStatus] = super.getSupportedStatuses ++ Set(taskStarted, writeDatabase)

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(new SetupDatabase(table, taskConfig.get.getBoolean("initiallyBroken")))
    case `writeDatabase` => Some(new WriteDatabase(table))
    case _ => super.getHandler(status)
  }

}
