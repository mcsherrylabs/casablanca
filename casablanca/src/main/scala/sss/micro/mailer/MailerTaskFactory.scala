package sss.micro.mailer

import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import courier._
import courier.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.StatusUpdate
import casablanca.task.RelativeScheduledStatusUpdate
import java.util.concurrent.TimeUnit
import scala.util.Success
import scala.util.Failure
import casablanca.task.TaskStatus
import scala.language.reflectiveCalls
import casablanca.webservice.remotetasks.RemotedTaskHandlerFactory

object MailHandler extends TaskHandler {

  val maxAttemptCount = 5

  private val mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as("alanmcsherry@gmail.com", "cuimuxmqnucnmiuh")
    .startTtls(true)()

  private def mailIt(task: Task): StatusUpdate = {

    val f = mailer(Envelope.from("alanmcsherry" at "gmail.com")
      .to("alan" `@` "mcsherrylabs.com")
      .subject("MAiler Task")
      .content(Text("Hello there business task in progress")))

    Await.result(f, Duration(60, TimeUnit.SECONDS))
    StatusUpdate(taskFinished)
  }

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    mailIt(task)
  }

}

object MailerTaskFactory extends RemotedTaskHandlerFactory {

  val mailerTaskType = "mailerTask"
  def getTaskType: String = mailerTaskType

  override def getSupportedStatuses: Set[Int] = super.getSupportedStatuses ++ Set(taskStarted)
  override def getHandler[T >: TaskHandler](status: Int): Option[T] = status match {
    case `taskStarted` => Some(MailHandler)
    case _ => super.getHandler(status)
  }

  def startTask(taskHandler: TaskHandlerContext,
    parentNode: Option[String],
    parentTaskId: Option[String],
    strPayload: String): Task = {
    taskHandler.startTask(mailerTaskType, taskStarted, strPayload, 0, parentTaskId, parentNode)
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: String): Option[StatusUpdate] = {
    println(s"MailerTaskFactory Got something back from mailer: ${event}")
    None
  }
}