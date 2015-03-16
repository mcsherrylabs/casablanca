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
import casablanca.sss.demo.DemoTaskFactory
import casablanca.util.Logging
<<<<<<< HEAD
import casablanca.sss.demo.BrokenTaskFactory
=======
import casablanca.sss.demo.LoadTestTaskFactory
>>>>>>> b9773b2db4d211162e0ae379eac319386082746c

object App {

  def main(args: Array[String]): Unit = {

    val configName = if (args.size > 0) {
      args(0)
    } else "main"

    val (row: Int, col: Int) = if (args.size > 2) {
      (args(1).toInt, args(2).toInt)
    } else (0, 0)

    val thf = TaskHandlerFactoryFactory(MailerTaskFactory,
      RemoteTaskHandlerFactory,
      DomainTransferHandlerFactory,
      BrokenTaskFactory,
      LoadTestTaskFactory,
      new DemoTaskFactory(row, col))

    println(s"This instance supports the following task factories and statuses...")
    thf.supportedFactories.foreach { fact =>
      println(s"${fact.getTaskType}  ${fact.getSupportedStatuses}")
    }

    new WorkflowManagerImpl(thf, configName).start

  }
}
