package clerk.flows.transfer

import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate
import DomainTransferConsts._
import casablanca.handler.StatusHandler

class FinaliseTransferHandler extends StatusHandler {

  def handle(task: Task): HandlerUpdate	= {
     println("Transfer accepted and complete! ")
     StatusUpdate(transferTaskComplete)
  }
}