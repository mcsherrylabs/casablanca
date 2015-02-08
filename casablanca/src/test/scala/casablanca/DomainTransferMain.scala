package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.handler.StatusUpdate
import clerk.flows.transfer.DomainTransferHandlerFactory
import clerk.flows.transfer.InitialiseTransfer
import clerk.flows.transfer.DomainTransferConsts

object DomainTransferMain {

  def main(args: Array[String]): Unit = {
   
    val tm = new TaskManager("taskManager")
    val shf = new DomainTransferHandlerFactory
    val statusQManager = new StatusQueueManager(tm, shf)
    
    val scheduler = new Scheduler(tm, statusQManager, 10)
    
    scheduler.start
    
    val statusQueues = statusQManager.statusQueues
    statusQueues.map ( q => q.init)    	
   
    val transferStarter = new InitialiseTransfer(statusQManager)
    
    val transferTask = transferStarter.createTransferTask("domainName", "aspirantId", "ownerId")
    transferStarter.initTransfer(transferTask)
    
    println(s"Transfer Task Initialised ${transferTask}")
    while(tm.getTask(transferTask.id).status != DomainTransferConsts.awaitOwnerReponse) {      
      Thread.sleep(1000)
    }
    println(s"Now waiting for for onwer response... ")
    statusQManager.pushTask(tm.getTask(transferTask.id), StatusUpdate(DomainTransferConsts.acceptTransfer));
    println(s"Transfer Accepted ... ")
    
  }

}