package clerk.flows.transfer

import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate
import DomainTransferConsts._
import casablanca.handler.StatusHandler
import casablanca.handler.StatusUpdate

class UpdateRegistryHandler extends StatusHandler {

  def handle(task: Task): HandlerUpdate	= {
     println("Updating EPP with domain details (takes a second)")
     Thread.sleep(1000)
     StatusUpdate(acceptTransfer)
  }
}