package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandlerContext
import sss.micro.mailer.RemoteMailerTaskFactory
import casablanca.task.TaskParent

class InformOwnerHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    RemoteMailerTaskFactory.startRemoteTask(taskContext, "strPayload Inform owner", Some(TaskParent(task.id)))
    println(s"Informing owner ${task.strPayload}")
    StatusUpdate(awaitOwnerReponse.value)
  }
}