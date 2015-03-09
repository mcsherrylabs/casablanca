package casablanca

import com.twitter.finatra._
import casablanca.webservice.Endpoint
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL
import _root_.sss.micro.mailer.MailerTaskFactory
import casablanca.task.TaskManager
import clerk.flows.transfer.DomainTransferHandlerFactory
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.webservice.RestServer
import casablanca.util.ConfigureFactory
import casablanca.util.LogFactory
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory

object App {

  def main(args: Array[String]): Unit = {

    val restConfigName = if(args.size > 0) {
      args(0)
    } else "main"
      
    val thf = TaskHandlerFactoryFactory(MailerTaskFactory, RemoteTaskHandlerFactory, DomainTransferHandlerFactory)
    new WorkflowManagerImpl(thf, restConfigName).start
      

  }
}
