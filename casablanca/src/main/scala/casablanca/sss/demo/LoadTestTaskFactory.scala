package casablanca.sss.demo

import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import courier._
import courier.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.StatusUpdate
import casablanca.task.RelativeScheduledStatusUpdate
import java.util.concurrent.TimeUnit
import casablanca.task.TaskStatus
import scala.language.reflectiveCalls
import casablanca.task.TaskDescriptor
import casablanca.util.ProgrammingError
import casablanca.task.TaskParent
import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskEvent
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import java.net.URL
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory._
import scala.util.Random
import scala.annotation.tailrec
import casablanca.db.Db
import casablanca.db.Table
import casablanca.task.Success
import casablanca.db.Table

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
