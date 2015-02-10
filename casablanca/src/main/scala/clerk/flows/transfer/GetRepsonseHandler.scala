package clerk.flows.transfer

import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusUpdate
import DomainTransferConsts._
import casablanca.handler.StatusHandler

class GetResponseHandler extends StatusHandler {

  def handle(task: Task): HandlerUpdate	= {
     println("Accept Transfer yes/no?")
     readBoolean match {
       case true => StatusUpdate(updateRegistry)
       case x => StatusUpdate(rejectTransfer) 
     }
     
  }
}