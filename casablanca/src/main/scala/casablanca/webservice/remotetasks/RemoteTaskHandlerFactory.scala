package casablanca.webservice.remotetasks

import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import casablanca.task.ScheduledStatusUpdate
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.TaskStatus
import casablanca.webservice.remotetasks.RemoteTaskHelper._
import com.typesafe.config.ConfigFactory
import casablanca.task.TaskParent
import casablanca.task.TaskEvent
import casablanca.task.TaskDescriptor
import java.util.UUID

object RemoteTaskHandlerFactory extends BaseTaskHandlerFactory {

  import casablanca.webservice.remotetasks.RemoteTaskJsonMapper._

  case class RemoteTask(payload: String, node: String, taskType: String, taskId: String = UUID.randomUUID().toString)

  private val remoteTaskType = "remoteTask"
  def getTaskType: String = remoteTaskType

  override def getSupportedStatuses: Set[TaskStatus] = Set(taskStarted) ++ super.getSupportedStatuses

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = status match {
    case `taskStarted` => Some(RemoteRestRequestHandler)
    case unsupported => super.getHandler(status)
  }

  def startRemoteTask(taskContext: TaskHandlerContext, remoteTask: RemoteTask, parent: Task): Task = {
    val taskParent: Option[TaskParent] = Some(TaskParent(parent.id, Some(taskContext.nodeConfig.localNode)))
    startRemoteTask(taskContext, remoteTask, taskParent)
  }

  def startRemoteTask(taskContext: TaskHandlerContext, remoteTask: RemoteTask, parent: Option[TaskParent] = None): Task = {
    // add remote details    
    taskContext.startTask(TaskDescriptor(remoteTaskType, taskStarted, remoteTask), None,
      parent)
  }

}