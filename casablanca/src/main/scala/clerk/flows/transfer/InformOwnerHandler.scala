package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandlerContext
import casablanca.task.TaskParent
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory._

object InformOwnerHandler extends TaskHandler {

  var callbackTask: String = ""

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    val rem = RemoteTask("mailerNode", "mailerTask", "strPayload Inform owner")
    val t = RemoteTaskHandlerFactory.startRemoteTask(taskContext, rem, Some(TaskParent(task.id)))
    callbackTask = t.id
    StatusUpdate(awaitOwnerReponse.value)
  }
}