package clerk.flows.transfer

import casablanca.queues.StatusQueueManager
import casablanca.task.Task
import casablanca.WorkflowManager


class InitialiseTransfer(wfm: WorkflowManager) {

  import DomainTransferConsts._
  
  def createTransferTask(domainName: String, aspirantId: String, ownerId: String): Task = {
    wfm.createTask(domainTransferTaskType, initialiseTransfer, 
        Seq(domainName, aspirantId, ownerId).mkString(","), 0)            
  }
  
  def initTransfer(task: Task)  {            
    wfm.pushTask(task)    
  }
}