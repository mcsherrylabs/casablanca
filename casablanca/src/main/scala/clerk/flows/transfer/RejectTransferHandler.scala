package clerk.flows.transfer

import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate
import DomainTransferConsts._


class RejectTransferHandler extends StatusHandler {

  def handle(task: Task): HandlerUpdate	= {
     
    println(s"Transfer rejected ")
    StatusUpdate(transferTaskComplete)
  }
}