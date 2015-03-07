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
import casablanca.util.Configure
import spray.json._
import DefaultJsonProtocol._
import casablanca.task.TaskDescriptor
import casablanca.task.Success
import casablanca.util.Logging
import casablanca.task.SystemSuccess
import casablanca.task.TaskParent
import casablanca.task.AwaitEvent
import casablanca.task.EventOrigin
import casablanca.task.TaskEvent

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
      taskHandlerContext.nodeConfig.localNode,
      Some(task.id),
      None)

    val myUrl = new URL(taskHandlerContext.nodeConfig.map(remoteTaskCase.node) + "/task/decorated/" + remoteTaskCase.taskType.get)
    val p = POST(myUrl).addBody(newMsg)
    val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
    HandlerUpdate.awaitEvent
  }
}

object TaskDoneHandler extends RemoteRestHandler {

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    def callParentAsEvent: HandlerUpdate = {
      val origin = EventOrigin(task.id, task.taskType)
      val ev = TaskEvent(task.strPayload, Some(origin))
      taskHandlerContext.handleEvent(task.parentTaskId.get, ev)
      log.info(s"Parent informed - all done ${task.taskType} ${task.id} ")
      HandlerUpdate.systemSuccess
    }

    def callParentRemotely: HandlerUpdate = {
      val myUrl = new URL(task.parentNode.get + "/event/" + task.parentTaskId.get)
      log.debug("Calling remote parent ..." + myUrl)
      val decorated = fromRemoteTaskDecorator(task.strPayload, taskHandlerContext.nodeConfig.localNode, Some(task.id), Some(task.taskType))
      val p = POST(myUrl).addBody(decorated)
      val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
      log.info(s"Remote parent informed - all done ${task.taskType} ${task.id} ")
      HandlerUpdate.systemSuccess
    }

    if (task.status == taskFailed.value) {
      log.warn(s"Task ${task.id} has failed! ")
      log.warn(s"FAILED: ${task}")
    }

    (task.parentNode, task.parentTaskId) match {
      case (Some(parent), Some(taskId)) => callParentRemotely
      case (None, Some(taskId)) => callParentAsEvent
      case x => {
        log.info(s"No parent, all done ${task.taskType} ${task.id} ")
        HandlerUpdate.systemSuccess
      }
    }
  }
}
