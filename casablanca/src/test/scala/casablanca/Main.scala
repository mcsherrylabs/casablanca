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

object Main {

  def main(args: Array[String]): Unit = {

    val remoteDummy = new RemoteTaskHandlerFactory {
      override val remoteTask = RemoteTask("mailerNode", "dummyTask")   
    }
    
    val thf = TaskHandlerFactoryFactory(DummyTaskHandlerFactory, remoteDummy)

    new WorkflowManagerImpl(thf, args(0)).start

  }

}