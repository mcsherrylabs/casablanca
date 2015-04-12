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
import casablanca.task.TaskParent
import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskEvent
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import java.net.URL
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory._
import scala.util.Random
import casablanca.task.Success

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
