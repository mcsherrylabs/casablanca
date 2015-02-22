package sss.micro.mailer

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
import scala.util.Success
import scala.util.Failure
import casablanca.task.TaskStatus
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import casablanca.task.RemoteTask

object OnReponseHandler extends TaskHandler {

  val onRepsonse = 1001

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    println(s"Got ${task.strPayload} from mailer returning done")
    StatusUpdate(taskFinished)

  }
}

object RemoteMailerTaskFactory extends RemoteTaskHandlerFactory {

  import OnReponseHandler.onRepsonse

  override def getSupportedStatuses: Set[Int] = super.getSupportedStatuses ++ Set(onRepsonse)

  override def getHandler[T >: TaskHandler](status: Int): Option[T] = status match {
    case `onRepsonse` => Some(OnReponseHandler)
    case x => super.getHandler(status)
  }

  override def startRemoteTask(taskHandler: TaskHandlerContext, strPayload: String): Task = {
    // add remote details
    taskHandler.startTask(remoteTaskType, taskStarted, strPayload)
  }

  val remoteTask: RemoteTask = RemoteTask("localhost:7070", "mailerTask")
}