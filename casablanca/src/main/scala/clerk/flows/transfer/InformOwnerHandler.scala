package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._


class InformOwnerHandler extends TaskHandler {

  def handle(task: Task): HandlerUpdate	= {
     
    println(s"Informing owner ${task.strPayload}")
    StatusUpdate(awaitOwnerReponse)
  }
}