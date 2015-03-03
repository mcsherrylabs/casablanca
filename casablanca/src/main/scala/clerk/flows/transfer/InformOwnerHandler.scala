package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandlerContext

class InformOwnerHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    println(s"Informing owner ${task.strPayload}")
    StatusUpdate(awaitOwnerReponse.value)
  }
}