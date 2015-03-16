package casablanca.webservice

import scala.util.Try
import com.twitter.finatra._
import com.twitter.finatra.ContentType._
import spray.json._
import DefaultJsonProtocol._
import casablanca.WorkflowManager
import sss.micro.mailer.MailerTaskFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.TaskParent
import casablanca.task.TaskDescriptor
import casablanca.webservice.remotetasks.RemoteTaskHelper._
import casablanca.task.TaskEvent
import casablanca.task.EventOrigin
import casablanca.task.EventOrigin
import casablanca.util.Logging
import org.slf4j.Logger
import casablanca.util.LogFactory
import casablanca.task.TaskJsonMapper
import casablanca.task.Task
import scala.concurrent.Future

import casablanca.task.TaskJsonMapper._

class Distribute(taskContext: TaskHandlerContext, taskCompletionListener: TaskCompletionListener) extends Controller {

  private val myLog = LogFactory.getLogger(this.getClass.toString)


  //Provide the lastTimeStamp and UUID
  //mark that as distributed (delete it)
  //find the tasks after or equal to ts limit 2  
  get(s"/distribute/:nodeName/:taskType") { request =>
    request.routeParams.get("taskType") match {
      case None => {
        myLog.warn("No task id, cannot get task.")
        render.body("No task id! ").status(400).toFuture
      }
      case Some(tId) => {
        myLog.debug(s"Get task (${tId}) via endpoint... ")

        val str: String = taskContext.getTask(tId)
        render.status(200).body(str).toFuture
      }
    }
  }



  error { request =>
    request.error match {
      case Some(e: ArithmeticException) =>
        render.status(500).plain("whoops, divide by zero!").toFuture
      case Some(e: UnsupportedMediaType) =>
        render.status(415).plain("Unsupported Media Type!").toFuture
      case _ =>
        render.status(500).plain("Something went wrong!").toFuture
    }
  }

  /**
   * Custom 404s
   *
   * curl http://localhost:7070/notfound
   */
  notFound { request =>
    render.status(404).plain("not found").toFuture
  }

}