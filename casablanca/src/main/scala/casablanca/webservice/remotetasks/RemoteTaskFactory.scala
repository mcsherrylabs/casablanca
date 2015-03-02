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
import spray.json._
import DefaultJsonProtocol._

import casablanca.task.TaskDescriptor

object NodeConfig {
  val thisNode = "http://localhost:7070"
  def toUrl(node: String): URL = new URL("http://localhost:8282")
}

case class RemoteTaskDecorator(strPayload: String, node: String,
  taskId: Option[String] = None,
  taskType: Option[String] = None)

object RemoteTaskWithPayloadJsonProtocol extends DefaultJsonProtocol {
  implicit val remoteTaskWithPayloadFormat = jsonFormat4(RemoteTaskDecorator)
}

trait RemoteRestHandler extends TaskHandler with Configure {

  protected implicit val httpClient = new ApacheHttpClient
  protected val awaitRepsonse = 1000

}

object RemoteRestRequestHandler extends RemoteRestHandler {

  import RemoteTaskWithPayloadJsonProtocol._

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    val str = task.strPayload
    val js = str.parseJson
    println("IS " + js)
    val remoteTaskCase = js.convertTo[RemoteTaskDecorator]

    val json = RemoteTaskDecorator(remoteTaskCase.strPayload, NodeConfig.thisNode, Some(task.id), None).toJson

    val myUrl = new URL(remoteTaskCase.node + "/task/" + remoteTaskCase.taskType.get)
    val p = POST(myUrl).addBody(json.prettyPrint)
    val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
    StatusUpdate(awaitRepsonse)
  }
}

object RemoteRestResponseHandler extends RemoteRestHandler {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    val myUrl = new URL(task.parentNode.get + "/event/" + task.id)
    println("Firing back " + myUrl)
    val p = POST(myUrl).addBody(task.strPayload)
    val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
    StatusUpdate(systemSuccess)
  }
}

trait RemotedTaskHandlerFactory extends TaskHandlerFactory {

  def getSupportedStatuses: Set[Int] = Set(taskFinished)
  def getHandler[T >: TaskHandler](status: Int): Option[T] = status match {
    case `taskFinished` => Some(RemoteRestResponseHandler)
    case unsupported => None
  }

}

trait RemoteTaskHandlerFactory extends TaskHandlerFactory {

  import RemoteTaskWithPayloadJsonProtocol._
  val remoteTask: RemoteTask

  val remoteTaskType = "remoteTask"
  def getTaskType: String = remoteTaskType

  def getSupportedStatuses: Set[Int] = Set(taskStarted, taskFinished)
  def getHandler[T >: TaskHandler](status: Int): Option[T] = status match {
    case `taskStarted` => Some(RemoteRestRequestHandler)
    case `taskFinished` => Some(RemoteRestResponseHandler)
    case unsupported => None
  }

  def startRemoteTask(taskContext: TaskHandlerContext, strPayload: String): Task = {
    // add remote details
    val json = RemoteTaskDecorator(strPayload, remoteTask.node, None, Some(remoteTask.taskType)).toJson
    taskContext.startTask(TaskDescriptor(remoteTaskType, taskStarted, json.compactPrint))
  }

}