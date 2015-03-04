package casablanca.webservice.remotetasks

import casablanca.task.BaseTaskHandlerFactory
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
import scala.concurrent.Future
import casablanca.task.TaskStatus
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import casablanca.webservice.remotetasks.RemoteTaskHelper._
import com.typesafe.config.ConfigFactory
import casablanca.util.Configure
import spray.json._
import DefaultJsonProtocol._
import casablanca.task.TaskDescriptor
import casablanca.task.Success
import casablanca.util.Logging
import casablanca.task.SystemSuccess
import casablanca.task.TaskParent
import casablanca.task.AwaitEvent

trait RemoteRestHandler extends TaskHandler with Configure with Logging {

  protected implicit val httpClient = new ApacheHttpClient
  protected val awaitRepsonse = 1000

}

object RemoteRestRequestHandler extends RemoteRestHandler {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    val str = task.strPayload

    val remoteTaskCase = toRemoteTaskDecorator(str)

    val newMsg = fromRemoteTaskDecorator(
      remoteTaskCase.strPayload,
      NodeConfig.localNode,
      Some(task.id),
      None)

    val myUrl = new URL(NodeConfig.map(remoteTaskCase.node) + "/task/" + remoteTaskCase.taskType.get)
    val p = POST(myUrl).addBody(newMsg)
    val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
    HandlerUpdate.awaitEvent
  }
}

object TaskDoneHandler extends RemoteRestHandler {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    def callParentAsEvent: HandlerUpdate = {
      taskHandlerContext.handleEvent(task.parentTaskId.get, task.strPayload)
      SystemSuccess.apply
    }

    def callParentRemotely: HandlerUpdate = {
      val myUrl = new URL(task.parentNode.get + "/event/" + task.parentTaskId.get)
      println("Firing back " + myUrl)
      val p = POST(myUrl).addBody(task.strPayload)
      val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
      HandlerUpdate.systemSuccess
    }

    (task.parentNode, task.parentTaskId) match {
      case (Some(parent), Some(taskId)) => callParentRemotely
      case (None, Some(taskId)) => callParentAsEvent
      case x => {
        log.info("No parent, all Done ")
        HandlerUpdate.systemSuccess
      }
    }
  }
}

case class RemoteTask(node: String, taskType: String)

trait RemoteTaskHandlerFactory extends BaseTaskHandlerFactory {

  val remoteTask: RemoteTask

  private val remoteTaskType = "remoteTask"
  def getTaskType: String = remoteTaskType

  override def getSupportedStatuses: Set[TaskStatus] = Set(taskStarted) ++ super.getSupportedStatuses

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(RemoteRestRequestHandler)
    case unsupported => super.getHandler(status)
  }

  def startRemoteTask(taskContext: TaskHandlerContext, strPayload: String, parent: Option[TaskParent] = None): Task = {
    // add remote details
    val decorated = fromRemoteTaskDecorator(strPayload, remoteTask.node, None, Some(remoteTask.taskType))
    taskContext.startTask(TaskDescriptor(remoteTaskType, taskStarted, decorated), None,
      parent)
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: String): Option[HandlerUpdate] = {
    Some(HandlerUpdate.success)
  }

}