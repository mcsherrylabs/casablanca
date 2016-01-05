package sss.casablanca.webservice

import com.twitter.finatra.Controller
import sss.ancillary.LogFactory
import sss.casablanca.task.{ TaskHandlerContext, TaskManager }

/**
 * Work in progress!
 *
 * @param tm
 * @param taskContext
 * @param taskCompletionListener
 */
class Distribute(tm: TaskManager, taskContext: TaskHandlerContext, taskCompletionListener: TaskCompletionListener) extends Controller {

  import sss.casablanca.task.TaskJsonMapper._
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