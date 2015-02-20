package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandlerContext

class RejectTransferHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext,task: Task): HandlerUpdate	= {
     
    println(s"Transfer rejected ")
    StatusUpdate(transferTaskComplete)
  }
}