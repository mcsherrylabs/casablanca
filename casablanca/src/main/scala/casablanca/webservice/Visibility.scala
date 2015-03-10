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
import casablanca.task.TaskManager
import casablanca.task.Task

import casablanca.task.TaskJsonMapper._

class Visibility(tm: TaskManager) extends Controller {

  private val prefix = "/visibility"

  private val myLog = LogFactory.getLogger(this.getClass.toString)

  get(s"${prefix}/task/:taskId") { request =>
    request.routeParams.get("taskId") match {
      case None => {

        render.html(s"No task id given! ").status(400).toFuture
      }
      case Some(tId) => {
        render.status(200).html(tm.getTask(tId)).toFuture
      }
    }
  }

  get(s"${prefix}/tasktype/:tasktype/:status") { request =>

    try {
      request.routeParams.get("tasktype") match {
        case None => {
          render.html("No task type!").status(400).toFuture
        }
        case Some(tType) => {

          val statusOrAll = request.routeParams.get("status").map(_.toInt) match {
            case Some(-1) => None
            case x => Some(x)
          }

          val tasks = tm.findTasks(tType, statusOrAll.flatten)
          val html = toHtml(tasks)
          render.status(200).html(html).toFuture

        }

      }
    } catch {
      case e: Exception => {
        myLog.error("Couldn't process ", e)
        render.status(400).html(e.toString).toFuture
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

  private def toHtml(tasks: List[Task]): String = {
    val header = "<html><h1>Tasks list... </h1><table>"
    tasks.map { t =>
      s"<tr><td><a href ='${prefix}/task/${t.id}\'>${t.id}</a></td>" +
        s"<td>${t.status}</td>" +
        s"<td>${t.createTime}</td>" +
        s"<td>${t.attemptCount}</td>" +
        s"<td>${t.parentNode}</td>" +
        s"<td>${t.parentTaskId}</td>" +
        s"<td>${t.strPayload}</td> </tr>"

    }.foldLeft(header)((a, b) => a + b) + "</table><html>"
  }
}