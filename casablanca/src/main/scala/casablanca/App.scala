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
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.webservice.RestServer

import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import casablanca.sss.demo.DemoTaskFactory
import _root_.sss.ancillary.Logging
import casablanca.sss.demo.BrokenTaskFactory
import casablanca.sss.demo.LoadTestTaskFactory
import casablanca.task.TaskDescriptor
import casablanca.task.TaskStatus
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory.RemoteTask

object App extends Logging {

  def main(args: Array[String]): Unit = {

    val configName = if (args.size > 0) {
      args(0)
    } else "main"

    val (row: Int, col: Int) = if (args.size > 2) {
      (args(1).toInt, args(2).toInt)
    } else (0, 0)

    val dtf = new DemoTaskFactory(row, col)
    val thf = TaskHandlerFactoryFactory(MailerTaskFactory,
      RemoteTaskHandlerFactory,
      BrokenTaskFactory,
      LoadTestTaskFactory,
      dtf)

    println(s"This instance supports the following task factories and statuses...")
    thf.supportedFactories.foreach { fact =>
      println(s"${fact.getTaskType}  ${fact.getSupportedStatuses}")
    }

    val wfm = new WorkflowManagerImpl(thf, configName)
    wfm.start

    //val remoteTask = RemoteTask("1", "1_1", "demoTask")
    //val t = RemoteTaskHandlerFactory.startRemoteTask(wfm.statusQManager.taskContext, remoteTask, None)
    //log.info(s"Started ${t}")
    //val descriptor = TaskDescriptor("remoteTask", TaskStatus(102), "1")
    //wfm.statusQManager.taskContext.startTask(descriptor, None)

  }
}
