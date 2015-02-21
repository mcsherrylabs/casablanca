package clerk.flows.transfer

import org.scalatest._
import java.util.Date
import casablanca.task.TaskManager
import casablanca.queues.StatusQueueManager
import casablanca.task.StatusUpdate
import casablanca.queues.Scheduler
import casablanca.task.TaskHandlerFactoryFactory

class DomainTransferSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  
  "DomainTransferHandlerFactory " should " support all aspects of status sequentially " in {
    
    val tm = new TaskManager("taskManager")
    val domainTransferHandlerFactory = DomainTransferHandlerFactory
    val shf = TaskHandlerFactoryFactory(domainTransferHandlerFactory)
    val statusQManager = new StatusQueueManager(tm, shf)
    val scheduler = new Scheduler(tm, statusQManager, 10)
    
    val task = domainTransferHandlerFactory.createInitTask(statusQManager.taskContext , "domainName", "aspirantId", "ownerId")
    assert(task.status == DomainTransferConsts.initialiseTransfer)
    val initialiseTransferHndlr = domainTransferHandlerFactory.getHandler( task.status).get
    initialiseTransferHndlr.handle(statusQManager.taskContext, task) match {
      case StatusUpdate(DomainTransferConsts.awaitOwnerReponse, _, _, _) => //horray!
      case x => fail("Taint right")      
    }
    
    
  }
  
  
}