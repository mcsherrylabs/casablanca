package casablanca

import com.twitter.finatra._
import casablanca.webservice.Endpoint
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import casablanca.task.TaskManager
import clerk.flows.transfer.DomainTransferHandlerFactory
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import sss.micro.mailer.MailerTaskFactory

object App extends FinatraServer {

  val tm = new TaskManager("taskManager")

  //val domainTransferHandlerFactory = new DomainTransferHandlerFactory(tm)
  val shf = TaskHandlerFactoryFactory(DomainTransferHandlerFactory, MailerTaskFactory)

  val statusQManager = new StatusQueueManager(tm, shf)
  val scheduler = new Scheduler(tm, statusQManager, 10)

  val workflowManager: WorkflowManager = new WorkflowManagerImpl(tm,
    statusQManager,
    shf,
    scheduler)

  workflowManager.start

  register(new Endpoint(statusQManager.taskContext))
}
