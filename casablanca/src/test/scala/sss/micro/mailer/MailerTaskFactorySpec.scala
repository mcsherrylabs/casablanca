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
import casablanca.util.ConfigureFactory
import casablanca.WorkflowManagerImpl
import MailJsonMapper._

class MailerTaskFactoySpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "MailerTaskFactoySpec " should " support all aspects of status sequentially " in {

    val shf = TaskHandlerFactoryFactory(MailerTaskFactory)
    val wfm = new WorkflowManagerImpl(shf)
    val tc = wfm.statusQManager.taskContext
    val mail = Mail(Email("alan", "gmail.com"), Email("sdfsdf", "sds.com"), "", "")
    println("MAIL " + mail)
    val task = MailerTaskFactory.mail(tc, mail)
    assert(task.status == MailerTaskFactory.taskStarted.value)
    val mailer = MailerTaskFactory.getHandler(TaskStatus(task.status))
    mailer.get.handle(tc, task) match {
      case su: HandlerUpdate => assert(
        MailerTaskFactory.getHandler(TaskStatus(su.nextStatus.get))
          == Some(TaskDoneHandler))
      case x => fail("Wrong type")
    }

  }

}