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

class Endpoint(taskContext: TaskHandlerContext) extends Controller {

  post(s"/task/:taskType") { request =>

    println(s"REQ: ${request.contentString}")
    request.routeParams.get("taskType") match {
      case None => render.body("No task type! ").status(400).toFuture
      case Some(tType) => {
        val str = request.contentString
        val remoteTaskCase = toRemoteTaskDecorator(str)
        taskContext.startTask(
          TaskDescriptor(tType, taskContext.taskStarted, remoteTaskCase.strPayload),
          None,
          Some(TaskParent(remoteTaskCase.taskId.get, Some(remoteTaskCase.node))))

        render.status(200).toFuture

      }

    }
  }

  post(s"/event/:taskId") { request =>

    println(s"EVENT: ${request.contentString}")
    request.routeParams.get("taskId") match {
      case None => render.body("No task id! ").status(400).toFuture
      case Some(tId) => {
        val remoteTaskCase = toRemoteTaskDecorator(request.contentString)
        val origin = EventOrigin(remoteTaskCase.taskId.get, remoteTaskCase.taskType.get)
        val ev = TaskEvent(remoteTaskCase.strPayload, Some(origin))
        taskContext.handleEvent(tId, ev)
        render.status(200).toFuture
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
    render.status(404).plain("not found yo").toFuture
  }

}