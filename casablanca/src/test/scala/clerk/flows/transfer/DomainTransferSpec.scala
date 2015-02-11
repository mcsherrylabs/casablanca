package clerk.flows.transfer

import org.scalatest._
import java.util.Date
import casablanca.task.TaskManager
import casablanca.queues.StatusQueueManager
import casablanca.handler.StatusUpdate
import casablanca.queues.Scheduler
import casablanca.WorkflowManagerImpl

class DomainTransferSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  
  "DomainTransferHandlerFactory " should " support all aspects of status sequentially " in {
    
    val tm = new TaskManager("taskManager")
    val domainTransferHandlerFactory = new DomainTransferHandlerFactory
    val statusQManager = new StatusQueueManager(tm, domainTransferHandlerFactory)
    val scheduler = new Scheduler(tm, statusQManager, 10)
    
    val workflowManager = new WorkflowManagerImpl(tm,
    statusQManager,
    domainTransferHandlerFactory, 
    scheduler)
   
    val initialiseTransfer = new InitialiseTransfer(workflowManager)
    val task = initialiseTransfer.createTransferTask("domainName", "aspirantId", "ownerId")
    assert(task.status == DomainTransferConsts.initialiseTransfer)
    val initialiseTransferHndlr = domainTransferHandlerFactory.getHandler(task.status).get
    initialiseTransferHndlr.handle(task) match {
      case StatusUpdate(DomainTransferConsts.awaitOwnerReponse, _, _, _) => //horray!
      case x => fail("Taint right")      
    }
    
    
  }
  
  
}