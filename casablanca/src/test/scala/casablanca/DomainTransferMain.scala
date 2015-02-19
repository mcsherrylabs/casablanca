package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.StatusUpdate
import clerk.flows.transfer.DomainTransferHandlerFactory
import clerk.flows.transfer.DomainTransferConsts
import clerk.flows.transfer.DomainTransferTask
import casablanca.task.Task
import casablanca.db.Row
import casablanca.task.TaskHandlerFactoryFactory

object DomainTransferMain {

  def main(args: Array[String]): Unit = {
   
    val tm = new TaskManager("taskManager") 
    
    val domainTransferHandlerFactory = new DomainTransferHandlerFactory(tm)
    val shf = TaskHandlerFactoryFactory(domainTransferHandlerFactory)
    
    val statusQManager = new StatusQueueManager(tm, shf)   
    val scheduler = new Scheduler(tm, statusQManager, 120)
    
    val workflowManager = new WorkflowManagerImpl(tm,
    statusQManager,
    shf, 
    scheduler)
   
    workflowManager.start
    
    val transferStarter = domainTransferHandlerFactory.createInitTask("domainName", "aspirantId", "ownerId")
   
    workflowManager.pushTask(transferStarter)
    
    
    //println(s"Transfer Task Initialised ${transferTask}")
    //while(tm.getTask(transferTask.id).status != DomainTransferConsts.awaitOwnerReponse) {      
    //  Thread.sleep(1000)
    //}
    //println(s"Now waiting for for onwer response... ")
    //statusQManager.pushTask(tm.getTask(transferTask.id), StatusUpdate(DomainTransferConsts.acceptTransfer));
    //println(s"Transfer Accepted ... ")
    
  }

}