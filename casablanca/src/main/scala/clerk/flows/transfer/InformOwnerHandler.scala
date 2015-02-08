package clerk.flows.transfer

import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate
import DomainTransferConsts._


class InformOwnerHandler extends StatusHandler {

  def handle(task: Task): HandlerUpdate	= {
     
    println(s"Informing owner ${task.strPayload}")
    StatusUpdate(awaitOwnerReponse)
  }
}