package sss.mailer

import javax.mail.internet.InternetAddress

import sss._
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import courier._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.StatusUpdate
import java.util.concurrent.TimeUnit
import casablanca.task.TaskStatus
import scala.language.reflectiveCalls
import casablanca.task.TaskParent
import casablanca.task.BaseTaskHandlerFactory

import scala.collection.JavaConversions._
import casablanca.util.ProgrammingError
import casablanca.task.TaskDescriptor

case class Email(name: String, domain: String)
case class Mail(from: Email, to: Email, subject: String, body: String, mailerConfigName: String = "default")

class MailHandler(mailers: Map[String, Mailer]) extends TaskHandler {

  import MailJsonMapper._
  import courier.Defaults._

  private def mailIt(task: Task): HandlerUpdate = {

    val mail: Mail = task.strPayload

    def t(msg: String) = throw new ProgrammingError(msg)

    //.to.name `@` mail.to.domain
    val w = Envelope.from(mail.from.name at mail.from.domain).to(mail.to.name `@` mail.to.domain)
      .subject(mail.subject)
      .content(Text(mail.body))

    val f = mailers.getOrElse(mail.mailerConfigName, t(s"No such mail config -> ${mail.mailerConfigName}"))(w)

    Await.result(f, Duration(60, TimeUnit.SECONDS))
    StatusUpdate(taskFinished.value)
  }

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = mailIt(task)

}

object MailerTaskFactory extends BaseTaskHandlerFactory {

  import MailJsonMapper._

  lazy val mailers = taskConfig.get.getConfigList("mailConfigs").map { c =>

    (c.getString("name") ->
      Mailer(c.getString("server"), c.getInt("port"))
      .auth(c.getBoolean("auth"))
      .as(c.getString("user"), c.getString("pass"))
      .startTtls(c.getBoolean("startTtls"))())

  }.toMap

  val mailerTaskType = "mailerTask"
  def getTaskType: String = mailerTaskType

  override def getSupportedStatuses: Set[TaskStatus] = super.getSupportedStatuses ++ Set(taskStarted)
  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(new MailHandler(mailers))
    case _ => super.getHandler(status)
  }

  def mail(taskContext: TaskHandlerContext, mail: Mail, parent: Option[TaskParent] = None): Task = {

    val taskDescriptor = TaskDescriptor(mailerTaskType, taskStarted, mail)
    taskContext.startTask(taskDescriptor, None, parent)
  }
}