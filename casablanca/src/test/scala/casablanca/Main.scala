package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.StatusUpdate
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.task.TaskDescriptor

object Main {

  def main(args: Array[String]): Unit = {

    val tm = new TaskManager("taskManager")
    val gsf = new GenericStatusHandlerFactory(tm)
    val shf = TaskHandlerFactoryFactory(gsf)

    val statusQManager = new StatusQueueManager(tm, shf)

    val scheduler = new Scheduler(tm, statusQManager, 10)

    val workflowManager = new WorkflowManagerImpl(tm,
      statusQManager,
      shf,
      scheduler)

    workflowManager.start

    val t = statusQManager.taskContext.startTask(TaskDescriptor(gsf.getTaskType, 0, "strPayload"))

    //   tm.create("taskType", 2, "strPayload2", 33)
    //   tm.create("taskType", 2, "strPayload2", 33)
    //   tm.create("taskType", 2, "strPayload2", 33)
    //   tm.create("taskType", 2, "strPayload2", 33)

  }

}