package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandler
import casablanca.task.TaskHandlerContext

class GetResponseHandler extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    println("Accept Transfer yes/no?")
    readBoolean match {
      case true => StatusUpdate(updateRegistry.value)
      case x => StatusUpdate(rejectTransfer.value)
    }

  }
}