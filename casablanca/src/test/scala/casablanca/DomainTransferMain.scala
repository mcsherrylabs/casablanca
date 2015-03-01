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
import casablanca.task.TaskHandlerContext
import java.util.Date

object DomainTransferMain {

  def main(args: Array[String]): Unit = {

    val tm = new TaskManager("taskManager")
    val gsf = new GenericStatusHandlerFactory(tm)
    //val domainTransferHandlerFactory = new DomainTransferHandlerFactory(tm)
    val shf = TaskHandlerFactoryFactory(DomainTransferHandlerFactory, gsf)

    val statusQManager = new StatusQueueManager(tm, shf)
    val scheduler = new Scheduler(tm, statusQManager, 10)

    val workflowManager: WorkflowManager = new WorkflowManagerImpl(tm,
      statusQManager,
      shf,
      scheduler)

    workflowManager.start

    val transferStarter = DomainTransferHandlerFactory.createInitTask(statusQManager.taskContext, "domainName", "aspirantId", "ownerId")
    val t = statusQManager.taskContext.startTask(gsf.getTaskType, 0, "strPayload", 33)

  }

}