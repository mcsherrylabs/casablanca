package sss.casablanca.webservice.remotetasks

import java.net.URL

import _root_.sss.ancillary.{ Configure, Logging }
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import com.stackmob.newman.response.HttpResponseCode
import sss.casablanca.task.{ EventOrigin, HandlerUpdate, Task, TaskEvent, TaskHandler, TaskHandlerContext }
import sss.casablanca.webservice.remotetasks.RemoteTaskHandlerFactory.RemoteTask
import sss.casablanca.webservice.remotetasks.RemoteTaskHelper._

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.reflectiveCalls

trait RemoteRestHandler extends TaskHandler with Configure with Logging {

  protected implicit val httpClient = new ApacheHttpClient
  protected val awaitRepsonse = 1000

}

object RemoteRestRequestHandler extends RemoteRestHandler {

  import sss.casablanca.webservice.remotetasks.RemoteTaskJsonMapper._

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    log.debug("P " + task.strPayload)
    val remoteTask: RemoteTask = task.strPayload

    val remoteTaskDecorator = RemoteTaskDecorator(
      remoteTask.payload,
      taskHandlerContext.nodeConfig.localNode,
      task.parentTaskId,
      remoteTask.taskId,
      remoteTask.taskType)

    import sss.casablanca.webservice.remotetasks.RemoteTaskHelper._

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

