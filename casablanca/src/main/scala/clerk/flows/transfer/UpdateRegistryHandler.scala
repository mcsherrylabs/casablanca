package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandler
import casablanca.task.StatusUpdate
import casablanca.task.TaskHandlerContext

class UpdateRegistryHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext,task: Task): HandlerUpdate	= {
     println("Updating EPP with domain details (takes a second)")
     Thread.sleep(1000)
     StatusUpdate(acceptTransfer)
  }
}