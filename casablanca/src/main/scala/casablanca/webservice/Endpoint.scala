package casablanca.webservice

import scala.util.Try
import com.twitter.finatra._
import com.twitter.finatra.ContentType._
import spray.json._
import DefaultJsonProtocol._
import casablanca.WorkflowManager
import sss.micro.mailer.MailerTaskFactory
import casablanca.task.TaskHandlerContext

class Endpoint(taskContext: TaskHandlerContext) extends Controller {

  /**
   * Uploading files
   */
  post(s"/task/${MailerTaskFactory.getTaskType}") { request =>

    println(s"REQ: ${request.contentString}")
    MailerTaskFactory.startTask(taskContext, request.contentString)
    render.status(200).toFuture //.json(json.compactPrint).toFuture

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
    render.status(404).plain("not found yo").toFuture
  }

}