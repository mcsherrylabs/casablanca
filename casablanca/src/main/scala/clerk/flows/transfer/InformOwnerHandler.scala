package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandlerContext
import sss.micro.mailer.RemoteMailerTaskFactory
import casablanca.task.TaskParent

object InformOwnerHandler extends TaskHandler {

  var callbackTask: String = ""

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    val t = RemoteMailerTaskFactory.startRemoteTask(taskContext, "strPayload Inform owner", Some(TaskParent(task.id)))
    callbackTask = t.id
    StatusUpdate(awaitOwnerReponse.value)
  }
}