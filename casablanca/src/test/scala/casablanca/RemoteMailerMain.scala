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
import sss.micro.mailer.RemoteMailerTaskFactory
import casablanca.webservice.RestServer

object RemoteMailerMain {

  def main(args: Array[String]): Unit = {

    val tm = new TaskManager("taskManager")
    val gsf = new GenericStatusHandlerFactory(tm)
    //val domainTransferHandlerFactory = new DomainTransferHandlerFactory(tm)
    val shf = TaskHandlerFactoryFactory(RemoteMailerTaskFactory)

    val statusQManager = new StatusQueueManager(tm, shf)
    val scheduler = new Scheduler(tm, statusQManager, 10)

    val restServer = new RestServer()

    val workflowManager: WorkflowManager = new WorkflowManagerImpl(tm,
      statusQManager,
      shf,
      scheduler,
      restServer)

    workflowManager.start

    val mail = RemoteMailerTaskFactory.startRemoteTask(statusQManager.taskContext, "MAIL THIS")

  }

}