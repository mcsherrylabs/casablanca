package casablanca.webservice.remotetasks

import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import courier._
import courier.Defaults._
import scala.language.reflectiveCalls
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.StatusUpdate
import casablanca.task.ScheduledStatusUpdate
import casablanca.task.RelativeScheduledStatusUpdate
import java.util.concurrent.TimeUnit
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future
import casablanca.task.RemoteTask
import casablanca.task.TaskStatus
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import casablanca.task.RemoteTask
import com.typesafe.config.ConfigFactory
import casablanca.util.Configure

object RemoteRestHandler extends TaskHandler with Configure {

  private implicit val httpClient = new ApacheHttpClient
  val awaitRepsonse = 1000

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    //execute a GET request
    //config.getString(emoteTask.)
    val myUrl = new URL("http://google.com")
    val response = Await.result(GET(myUrl).apply, 10.second) //this will throw if the response doesn't return within 1 second
    StatusUpdate(awaitRepsonse)
  }
}

trait RemoteTaskHandlerFactory extends TaskHandlerFactory {

  val remoteTask: RemoteTask

  val remoteTaskType = "remoteTask"
  def getTaskType: String = remoteTaskType

  def getSupportedStatuses: Set[Int] = Set(taskStarted)
  def getHandler[T >: TaskHandler](status: Int): Option[T] = status match {
    case `taskStarted` => Some(RemoteRestHandler)
    case unsupported => None
  }

  def startRemoteTask(taskHandler: TaskHandlerContext, strPayload: String): Task = {
    taskHandler.startTask(remoteTaskType, taskStarted, strPayload)
  }

}