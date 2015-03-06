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
import casablanca.webservice.RestServer
import casablanca.util.ConfigureFactory
import casablanca.util.LogFactory

object App {

  def main(args: Array[String]): Unit = {

    val tm = new TaskManager("taskManager")

    val shf = TaskHandlerFactoryFactory(DomainTransferHandlerFactory, MailerTaskFactory)

    val statusQManager = new StatusQueueManager(tm, shf)
    val loopTime = ConfigureFactory.config.getInt("schedulerGranularityInSeconds")
    val scheduler = new Scheduler(tm, statusQManager, loopTime)

    val restServer = new RestServer((new Endpoint(statusQManager.taskContext)))

    val workflowManager: WorkflowManager = new WorkflowManagerImpl(tm,
      statusQManager,
      shf,
      scheduler,
      restServer)

    workflowManager.start

  }
}
