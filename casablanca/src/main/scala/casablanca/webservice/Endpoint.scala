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

class Endpoint(taskContext: TaskHandlerContext, taskCompletionListener: TaskCompletionListener) extends Controller {

  private val myLog = LogFactory.getLogger(this.getClass.toString)

  get(s"/task/:taskId") { request =>
    request.routeParams.get("taskId") match {
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

  post(s"/task/:taskType") { request =>

    try {
      request.routeParams.get("taskType") match {
        case None => {
          myLog.warn("No task type, cannot process task create!!")
          render.body("No task type! ").status(400).toFuture
        }
        case Some(tType) => {
          myLog.debug(s"Starting task (${tType}) via endpoint... ")
          val str = request.contentString

          // Note task may finish in gap between task started
          // and time listener is set up...., so separate out create
          // and push
          val t = taskContext.create(
            TaskDescriptor(tType, taskContext.taskStarted, str),
            None,
            None)

          val mininmumWaitTimeMs = request.request.getIntParam("wait", 0)
          val twitFuture = taskCompletionListener.listenForCompletion[ResponseBuilder](t,
            tsk => render.status(200).body((tsk)),
            Some(mininmumWaitTimeMs))

          // pushing the task allows it to start...    
          taskContext.pushTask(t)
          twitFuture
        }

      }
    } catch {
      case e: Exception => {
        myLog.error("Couldn't process post /task", e)
        throw e
      }
    }
  }

  post(s"/task/decorated/:taskType") { request =>

    try {
      request.routeParams.get("taskType") match {
        case None => {
          myLog.warn("No task type, cannot process task create!!")
          render.body("No task type! ").status(400).toFuture
        }
        case Some(tType) => {
          myLog.debug(s"Starting task (${tType}) via endpoint... ")
          val str = request.contentString
          val remoteTaskCase = toRemoteTaskDecorator(str)
          taskContext.startTask(
            TaskDescriptor(tType, taskContext.taskStarted, remoteTaskCase.strPayload),
            None,
            Some(TaskParent(remoteTaskCase.taskId.get, Some(remoteTaskCase.node))))

          render.status(200).toFuture

        }

      }
    } catch {
      case e: Exception => {
        myLog.error("Couldn't process post /task/decorated", e)
        throw e
      }
    }
  }

  post(s"/event/:taskId") { request =>

    request.routeParams.get("taskId") match {
      case None => {
        myLog.warn("No task id, cannot process event!")
        render.body("No task id! ").status(400).toFuture
      }
      case Some(tId) => {
        myLog.debug(s"Processing event (${tId}) via endpoint... ")
        val remoteTaskCase = toRemoteTaskDecorator(request.contentString)
        val origin = EventOrigin(remoteTaskCase.taskId.get, remoteTaskCase.taskType.get)
        val ev = TaskEvent(remoteTaskCase.strPayload, Some(origin))
        //myLog.debug(s"EVENT")
        //myLog.debug(s"${ev}")
        //myLog.debug(s"END EVENT")
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