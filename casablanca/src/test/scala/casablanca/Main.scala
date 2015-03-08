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
import casablanca.util.LogFactory

object Main {

  def main(args: Array[String]): Unit = {

    /*val remoteDummy = new RemoteTaskHandlerFactory {
      override val remoteTask = RemoteTask("mailerNode", "dummyTask")   
    }
    val thf = TaskHandlerFactoryFactory(DummyTaskHandlerFactory, remoteDummy)
    */
    val thf = TaskHandlerFactoryFactory(new DemoTaskFactory(args(1).toInt, args(2).toInt), new RemoteTaskHandlerFactory {
               val remoteTask: RemoteTask = RemoteTask("nextNode", "demoTask")
    	  })

    LogFactory.getLogger("MAIN").info(s"Starting panel row ${args(1)} col ${args(1)}")
    new WorkflowManagerImpl(thf, args(0)).start

  }

}