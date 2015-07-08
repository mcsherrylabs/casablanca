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
import _root_.sss.ancillary.Logging
import org.slf4j.Logger
import _root_.sss.ancillary.LogFactory
import casablanca.task.TaskJsonMapper
import casablanca.task.Task
import scala.concurrent.Future

import casablanca.task.TaskManager

class Distribute(tm: TaskManager, taskContext: TaskHandlerContext, taskCompletionListener: TaskCompletionListener) extends Controller {

  import casablanca.task.TaskJsonMapper._
  private val myLog = LogFactory.getLogger(this.getClass.toString)

  put("/distribute/:nodeName/:lastTaskId") { request =>
    request.routeParams.get("nodeName") match {
      case None => {
        myLog.warn("No node name, cannot get task.")
        render.body("No node name ! ").status(400).toFuture
      }
      case Some(nodeName) => {
        request.routeParams.get("lastTaskId") match {
          case None => {
            myLog.warn("No last task id , cannot get task.")
            render.body("No last task id! ").status(400).toFuture
          }
          case Some(lastTaskId) => {
            val task = tm.getTask(lastTaskId)
            val res = tm.deleteTask(lastTaskId)
            render.status(200).body(res.toString).toFuture
          }
        }
      }
    }
  }
  //Provide the lastTimeStamp and UUID
  //mark that as distributed (delete it)
  //find the tasks after or equal to ts limit 2  
  get(s"/distribute/:nodeName/:taskType/:lastTaskId") { request =>
    request.routeParams.get("nodeName") match {
      case None => {
        myLog.warn("No node name, cannot get task.")
        render.body("No task id! ").status(400).toFuture
      }
      case Some(tId) => {
        myLog.debug(s"Get task (${tId}) via endpoint... ")

        val str: String = taskContext.getTask(tId)
        render.status(200).body(str).toFuture
      }
    }
  }

}