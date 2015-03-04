package sss.micro.mailer

import org.scalatest._
import java.util.Date
import casablanca.task.TaskManager
import casablanca.queues.StatusQueueManager
import casablanca.task.StatusUpdate
import casablanca.queues.Scheduler
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.task.TaskStatus
import casablanca.task.TaskDescriptor
import casablanca.task.TaskStatus
import casablanca.webservice.remotetasks.TaskDoneHandler
import casablanca.task.HandlerUpdate

class MailerTaskFactoySpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "MailerTaskFactoySpec " should " support all aspects of status sequentially " in {

    val tm = new TaskManager("taskManager")
    val mailerTaskFactory = MailerTaskFactory
    val shf = TaskHandlerFactoryFactory(mailerTaskFactory)
    val statusQManager = new StatusQueueManager(tm, shf)
    val scheduler = new Scheduler(tm, statusQManager, 10)
    val tc = statusQManager.taskContext
    val task = tc.startTask(TaskDescriptor(mailerTaskFactory.getTaskType, tc.taskStarted, ""))
    assert(task.status == mailerTaskFactory.taskStarted.value)
    val mailer = mailerTaskFactory.getHandler(TaskStatus(task.status))
    mailer.get.handle(tc, task) match {
      case su: HandlerUpdate => assert(
        mailerTaskFactory.getHandler(TaskStatus(su.nextStatus.get))
          == Some(TaskDoneHandler))
      case x => fail("Wrong type")
    }

  }

}