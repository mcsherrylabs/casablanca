package casablanca.sss.demo

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
import casablanca.task.TaskDescriptor
import casablanca.task.TaskParent
import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskEvent
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import java.net.URL
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import scala.util.Random

trait DemoStatuses {
  val demoTask = "demoTask"
  val startNextTask = TaskStatus(1000) 
  val switchOffPanel = TaskStatus(1001)
}

abstract class BaseDemoHandler(row: Int, col: Int) extends TaskHandler with DemoStatuses {
  protected implicit val httpClient = new ApacheHttpClient 
}

class SwitchPanel(row: Int, col: Int, onOff: Boolean) extends BaseDemoHandler(row, col) {

  val b = onOff match {
    case true => "true"
    case false => "false"  
  }
  
  val url = new URL(s"http://localhost:7070/update/${row}/${col}/${b}")
  
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
	  GET(url).apply
	  if(onOff) {
	    // if turning on, we're at the beginning
	    log.info(s"Going to return startNextTask.value")
	    StatusUpdate(startNextTask.value)
	  } else StatusUpdate(taskFinished.value)
  }

}

class StartNextPanel(row: Int, col: Int) extends TaskHandler with DemoStatuses {

  private def nextNode: Option[String] = {
	    val nextRow = if(Random.nextBoolean) row + 1 else row - 1
	    val nextCol = if(Random.nextBoolean) col + 1 else col - 1
	    if(nextRow < 1 || nextRow > 4 || nextCol < 1 || nextCol > 4 ) None
	    else Some(s"${nextRow}_${nextCol}") 	      
	    
  	}
  
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    
    log.info(s"Going to start next panel ")
    nextNode match {
      case Some(nextNode) => {
    	  val nextPanel = new RemoteTaskHandlerFactory {
               val remoteTask: RemoteTask = RemoteTask(nextNode, demoTask)
    	  }
    	  nextPanel.startRemoteTask(taskHandlerContext, "", task)
    	  log.info(s"Next panel is ${nextNode}, awainting event")
    	  StatusUpdate(awaitingEvent.value)
      }
      case None => {
        log.info(s"No next panel!")
        Thread.sleep(5000) // just for effect
        StatusUpdate(switchOffPanel.value)
      }
    } 
    
  }
 
}

class DemoTaskFactory(row: Int, col: Int) extends BaseTaskHandlerFactory with DemoStatuses {

  def getTaskType: String = demoTask

  override def getSupportedStatuses: Set[TaskStatus] = super.getSupportedStatuses ++ Set(taskStarted, startNextTask, switchOffPanel)
  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(new SwitchPanel(row, col, true))
    case `startNextTask` => Some(new StartNextPanel(row, col))
    case `switchOffPanel` => Some(new SwitchPanel(row, col, false))
    case _ => super.getHandler(status)
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate] = {
    Some(StatusUpdate(switchOffPanel.value))
  }
}