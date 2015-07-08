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
import _root_.sss.ancillary.Logging
import org.slf4j.Logger
import _root_.sss.ancillary.LogFactory
import casablanca.task.TaskJsonMapper
import casablanca.task.Task
import scala.concurrent.Future
import casablanca.task.TaskJsonMapper
import com.twitter.util.{ Future => TwitFuture }

import casablanca.webservice.remotetasks.RemoteTaskDecorator

class Endpoint(taskContext: TaskHandlerContext, taskCompletionListener: TaskCompletionListener) extends Controller {

  import casablanca.webservice.remotetasks.RemoteTaskHelper._

  private val myLog = LogFactory.getLogger(this.getClass.toString)

  get(s"/task/:taskId") { request =>
    request.routeParams.get("taskId") match {
      case None => {
        myLog.warn("No task id, cannot get task.")
        render.body("No task id! ").status(400).toFuture
      }
      case Some(tId) => {
        myLog.debug(s"Get task (${tId}) via endpoint... ")

        val str: String = TaskJsonMapper.from(taskContext.getTask(tId))
        render.status(200).body(str).toFuture
      }
    }
  }

  post(s"/task") { request =>

    val remoteTaskCase: RemoteTaskDecorator = request.contentString
    myLog.debug(s"Starting task (${remoteTaskCase.taskType}, ${remoteTaskCase.taskId}) via endpoint... ")

    taskContext.findTask(remoteTaskCase.taskId) match {

      case None => {
        val t = taskContext.create(
          TaskDescriptor(remoteTaskCase.taskType, taskContext.taskStarted, remoteTaskCase.strPayload, Some(remoteTaskCase.taskId)),
          None,
          remoteTaskCase.parentTaskId map (parentTaskId => TaskParent(parentTaskId, Some(remoteTaskCase.node))))

        val mininmumWaitTimeMs = request.request.getIntParam("wait", 0)
        val twitFuture = taskCompletionListener.listenForCompletion[ResponseBuilder](t,
          tsk => render.status(200).body((TaskJsonMapper.from(tsk))),
          Some(mininmumWaitTimeMs))

        // pushing the task allows it to start...    
        taskContext.pushTask(t)
        twitFuture
      }
      case Some(tsk) => render.status(201).body((TaskJsonMapper.from(tsk))).toFuture
    }
  }

  post(s"/task/:taskType") { request =>

    myLog.debug("WE ARE POSTED")

    request.routeParams.get("taskType") match {
      case None => throw new RuntimeException(s"NO task type... ")
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
          tsk => render.status(200).body((TaskJsonMapper.from(tsk))),
          Some(mininmumWaitTimeMs))

        // pushing the task allows it to start...    
        taskContext.pushTask(t)
        twitFuture
      }
    }
  }

  post(s"/event") { request =>

    val remoteTaskCase: RemoteTaskDecorator = request.contentString
    myLog.debug(s"Processing event (${remoteTaskCase.parentTaskId}) via endpoint... ")
    val origin = EventOrigin(remoteTaskCase.taskId, remoteTaskCase.taskType)
    val ev = TaskEvent(remoteTaskCase.strPayload, Some(origin))
    taskContext.handleEvent(remoteTaskCase.parentTaskId.get, ev)
    render.status(200).toFuture

  }

  post(s"/event/:taskId") { request =>

    request.routeParams.get("taskId") match {
      case Some(tId) => {
        myLog.debug(s"Processing event (${tId}) via endpoint... ")
        val ev = TaskEvent(request.contentString, None)
        taskContext.handleEvent(tId, ev)
        render.status(200).toFuture
      }
      case None => {
        render.status(400).plain("No taskId included").toFuture
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