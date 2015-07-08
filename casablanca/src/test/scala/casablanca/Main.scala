package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.StatusUpdate
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.task.TaskDescriptor
import casablanca.task.TaskStatus
import casablanca.webservice.RestServer
import casablanca.testrigs.DummyTaskHandlerFactory
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import casablanca.sss.demo.DemoTaskFactory
import _root_.sss.ancillary.LogFactory
import casablanca.sss.demo.BrokenTaskFactory

object Main {

  def main(args: Array[String]): Unit = {

    val thf = TaskHandlerFactoryFactory(new DemoTaskFactory(args(1).toInt, args(2).toInt), RemoteTaskHandlerFactory, BrokenTaskFactory)

    LogFactory.getLogger("MAIN").info(s"Starting panel row ${args(1)} col ${args(2)}")
    new WorkflowManagerImpl(thf, args(0)).start

  }

}