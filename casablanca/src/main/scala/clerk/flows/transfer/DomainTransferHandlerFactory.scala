package clerk.flows.transfer

import casablanca.handler.StatusHandlerFactory
import casablanca.handler.StatusHandler
import casablanca.task.Task
import casablanca.task.TaskImpl
import casablanca.db.Row

/**
 * This will become a real boy ...
 */
class DomainTransferTask(row : Row) extends TaskImpl(row)


object DomainTransferConsts {
	val domainTransferTaskType = "domainTransfer" 
	val initialiseTransfer = 1
	val informOwner = 2
	val awaitOwnerReponse = 3
	val updateRegistry = 4	
	val acceptTransfer = 5
	val rejectTransfer = 6
	val cancelTransfer = 7
	val transferTaskComplete = 8
}

class DomainTransferHandlerFactory extends StatusHandlerFactory {

  import DomainTransferConsts._
  
  def getSupportedStatuses: List[Int] = {
    List(initialiseTransfer, updateRegistry, rejectTransfer, awaitOwnerReponse, acceptTransfer )
  }
  
  def getTaskType: String = domainTransferTaskType
    
  def getHandler[DomainTransferTask](status:Int) = {
    
    status match {
      case DomainTransferConsts.initialiseTransfer => Some(new InformOwnerHandler())
      case DomainTransferConsts.updateRegistry => Some(new UpdateRegistryHandler())
      case DomainTransferConsts.awaitOwnerReponse => Some(new GetResponseHandler())
      case DomainTransferConsts.acceptTransfer => Some(new FinaliseTransferHandler())
      case DomainTransferConsts.rejectTransfer => Some(new RejectTransferHandler())
      case _ => None
    }
    
  } 


}

