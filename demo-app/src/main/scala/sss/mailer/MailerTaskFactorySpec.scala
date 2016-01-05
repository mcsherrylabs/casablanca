package sss.mailer

import org.scalatest._
import sss.casablanca.WorkflowManagerImpl
import sss.casablanca.task.{ HandlerUpdate, TaskHandlerFactoryFactory, TaskStatus }
import sss.casablanca.webservice.remotetasks.TaskDoneHandler

class MailerTaskFactorySpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "MailerTaskFactorySpec " should " support all aspects of status sequentially " in {

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