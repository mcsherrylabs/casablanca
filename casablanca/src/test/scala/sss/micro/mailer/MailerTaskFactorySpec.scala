package sss.micro.mailer

import org.scalatest._
import java.util.Date
import casablanca.task.TaskManager
import casablanca.queues.StatusQueueManager
import casablanca.task.StatusUpdate
import casablanca.queues.Scheduler
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.task.TaskStatus

class MailerTaskFactoySpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "MailerTaskFactoySpec " should " support all aspects of status sequentially " in {

    val tm = new TaskManager("taskManager")
    val mailerTaskFactory = MailerTaskFactory
    val shf = TaskHandlerFactoryFactory(mailerTaskFactory)
    val statusQManager = new StatusQueueManager(tm, shf)
    val scheduler = new Scheduler(tm, statusQManager, 10)
    val tc = statusQManager.taskContext
    val task = mailerTaskFactory.startTask(tc, None, None, "")
    assert(task.status == mailerTaskFactory.taskStarted)
    val mailer = mailerTaskFactory.getHandler(task.status)
    mailer.get.handle(tc, task) match {
      case su: StatusUpdate => assert(mailerTaskFactory.getHandler(su.nextStatus) == None)
      case x => fail("Wrong type")
    }

  }

}