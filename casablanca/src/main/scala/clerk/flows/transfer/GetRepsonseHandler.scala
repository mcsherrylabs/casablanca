package clerk.flows.transfer

import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import DomainTransferConsts._
import casablanca.task.TaskHandler

class GetResponseHandler extends TaskHandler {

  def handle(task: Task): HandlerUpdate	= {
     println("Accept Transfer yes/no?")
     readBoolean match {
       case true => StatusUpdate(updateRegistry)
       case x => StatusUpdate(rejectTransfer) 
     }
     
  }
}