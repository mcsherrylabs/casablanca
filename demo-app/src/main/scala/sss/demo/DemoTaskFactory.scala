package sss.demo

import java.net.URL

import com.stackmob.newman._
import com.stackmob.newman.dsl._
import sss.casablanca.task.{ BaseTaskHandlerFactory, HandlerUpdate, StatusUpdate, Task, TaskEvent, TaskHandler, TaskHandlerContext, TaskStatus }
import sss.casablanca.webservice.remotetasks.RemoteTaskHandlerFactory._
import sss.casablanca.webservice.remotetasks.SimpleChildTaskTrackingPayloadJsonMapper._
import sss.casablanca.webservice.remotetasks.{ RemoteTaskHandlerFactory, SimpleChildTaskTrackingPayload }

import scala.annotation.tailrec
import scala.language.reflectiveCalls
import scala.util.Random

trait DemoStatuses {
  val demoTask = "demoTask"
  val startNextTask = TaskStatus(1000)
  val switchOffPanel = TaskStatus(1001)
  val remoteTaskKey = "myOneRemoteTask"
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

    if (onOff) {
      // if turning on, we're at the beginning
      GET(url).apply
      log.info(s"Going to return startNextTask.value")
      val remoteTaskId = taskHandlerContext.generateNewTaskId

      val payload: SimpleChildTaskTrackingPayload = try {
        task.strPayload.toInt
        SimpleChildTaskTrackingPayload(task.strPayload, Map(remoteTaskKey -> remoteTaskId))
      } catch {
        case e: NumberFormatException => {
          val update: SimpleChildTaskTrackingPayload = task.strPayload
          SimpleChildTaskTrackingPayload(update.payload, Map(remoteTaskKey -> remoteTaskId))
        }
      }

      StatusUpdate(startNextTask.value, Some(payload))
    } else {
      Thread.sleep(601)
      GET(url).apply
      StatusUpdate(taskFinished.value)
    }
  }

}

class StartNextPanel(minTailLen: Int, row: Int, col: Int) extends TaskHandler with DemoStatuses {

  @tailrec
  private def nextNode(tailSize: Int): Option[String] = {
    val nextRow = if (Random.nextBoolean) row + 1 else row - 1
    val nextCol = if (Random.nextBoolean) col + 1 else col - 1
    if (nextRow < 1 || nextRow > 4 || nextCol < 1 || nextCol > 4) {
      if (tailSize > minTailLen) None else nextNode(tailSize)
    } else Some(s"${nextRow}_${nextCol}")

  }

  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {

    log.info(s"Going to start next panel ")
    val simpleTask: SimpleChildTaskTrackingPayload = task.strPayload
    val tailSize = simpleTask.payload.toInt
    nextNode(tailSize) match {
      case Some(nextNodeRes) => {

        val payload = SimpleChildTaskTrackingPayload((tailSize + 1).toString, Map())
        val remoteTask = RemoteTask(payload, nextNodeRes, demoTask, simpleTask.remoteTasks.get(remoteTaskKey).get)

        RemoteTaskHandlerFactory.startRemoteTask(taskHandlerContext, remoteTask, task)
        log.info(s"Next panel is ${nextNodeRes}, awainting event")
        StatusUpdate(awaitingEvent.value)
      }
      case None => {
        log.info(s"No next panel!")
        Thread.sleep(601) // just for effect
        StatusUpdate(switchOffPanel.value)
      }
    }
  }

  override def reTry(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    taskHandlerContext.findChildren(task.id) match {
      case Nil => handle(taskHandlerContext, task)
      case child :: _ => StatusUpdate(awaitingEvent.value)
    }
  }
}

class DemoTaskFactory(row: Int, col: Int) extends BaseTaskHandlerFactory with DemoStatuses {

  def getTaskType: String = demoTask

  lazy val minTailLen = taskConfig.get.getInt("minTailLen")

  override def getSupportedStatuses: Set[TaskStatus] = super.getSupportedStatuses ++ Set(taskStarted, startNextTask, switchOffPanel)

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(new SwitchPanel(row, col, true))
    case `startNextTask` => Some(new StartNextPanel(minTailLen, row, col))
    case `switchOffPanel` => Some(new SwitchPanel(row, col, false))
    case _ => super.getHandler(status)
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate] = {
    log.debug(s"Consume Demo Task Event - ${task}, ${event}")
    val simpleTask: SimpleChildTaskTrackingPayload = task.strPayload
    val shouldMatch = simpleTask.remoteTasks.get(remoteTaskKey).get
    log.debug(s"Check - ${event.origin.get.taskId} == ${shouldMatch}")
    Some(StatusUpdate(switchOffPanel.value))
  }

}
