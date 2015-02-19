package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._


class RejectTransferHandler extends TaskHandler {

  def handle(task: Task): HandlerUpdate	= {
     
    println(s"Transfer rejected ")
    StatusUpdate(transferTaskComplete)
  }
}