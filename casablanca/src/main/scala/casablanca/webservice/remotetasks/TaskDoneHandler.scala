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
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory.RemoteTask
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
import com.stackmob.newman.response.HttpResponseCode

trait RemoteRestHandler extends TaskHandler with Configure with Logging {

  protected implicit val httpClient = new ApacheHttpClient
  protected val awaitRepsonse = 1000

}

object RemoteRestRequestHandler extends RemoteRestHandler {

  import casablanca.webservice.remotetasks.RemoteTaskJsonMapper._

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    log.debug("P " + task.strPayload)
    val remoteTask: RemoteTask = task.strPayload

    val remoteTaskDecorator = RemoteTaskDecorator(
      remoteTask.payload,
      taskHandlerContext.nodeConfig.localNode,
      task.parentTaskId,
      remoteTask.taskId,
      remoteTask.taskType)

    /*val newMsg = from(
      remoteTaskCase.strPayload,
      taskHandlerContext.nodeConfig.localNode,
      remoteTaskCase.parentTaskId,
      task.id,
      remoteTaskCase.taskType)*/

    import casablanca.webservice.remotetasks.RemoteTaskHelper._

    val debugNode = remoteTask.node

    val myUrl = new URL(taskHandlerContext.nodeConfig.map(debugNode) + "/task")
    val p = POST(myUrl).addBody(remoteTaskDecorator)
    log.debug(s"REMOTE POST ${myUrl}, ${remoteTaskDecorator}")
    val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
    if (response.code != HttpResponseCode.Ok) {
      if (response.code != HttpResponseCode.Created) {
        log.warn("This task is already created on remote side...!")
      } else throw new RuntimeException(s"Remote response is ${response.code} (not 200), ${response}")
    }
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
      val myUrl = new URL(task.parentNode.get + "/event")
      log.debug("Calling remote parent ..." + myUrl)
      val decorated = RemoteTaskDecorator(task.strPayload, taskHandlerContext.nodeConfig.localNode, task.parentTaskId, task.id, task.taskType)
      val p = POST(myUrl).addBody(decorated)
      val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
      if (response.code != HttpResponseCode.Ok) throw new RuntimeException(s"Remote response is ${response.code} (not 200), ${response}")
      log.info(s"Remote parent informed - all done ${task.taskType} ${task.id} ")
      HandlerUpdate.systemSuccess
    }

    if (task.status == taskFailed.value) {
      log.warn(s"Task ${task.id} has failed! ")
      log.warn(s"FAILED: ${task}")
    }

    // Inform any waiting req that it's finished
    taskHandlerContext.taskCompletionListener.complete(task)

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

