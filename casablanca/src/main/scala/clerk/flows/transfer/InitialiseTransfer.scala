package clerk.flows.transfer

import casablanca.queues.StatusQueueManager
import casablanca.task.Task


class InitialiseTransfer(sqm: StatusQueueManager) {

  import DomainTransferConsts._
  
  def createTransferTask(domainName: String, aspirantId: String, ownerId: String): Task = {
    sqm.createTask(domainTransferTaskType, initialiseTransfer, 
        Seq(domainName, aspirantId, ownerId).mkString(","), 0)            
  }
  
  def initTransfer(task: Task)  {            
    sqm.pushTask(task)    
  }
}