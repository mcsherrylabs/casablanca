package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandler
import casablanca.task.TaskHandlerContext

class FinaliseTransferHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    println("Transfer accepted and complete! ")
    StatusUpdate(transferTaskComplete.value)
  }
}