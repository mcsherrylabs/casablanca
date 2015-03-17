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

object RemoteTaskHandlerFactory extends BaseTaskHandlerFactory {

  case class RemoteTask(node: String, taskType: String, payload: String)
  //case class RemoteTask(node: String, taskType: String, remoteTaskId: String, payload: String)

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
    val decorated = fromRemoteTaskDecorator(remoteTask.payload, remoteTask.node, None, Some(remoteTask.taskType))
    taskContext.startTask(TaskDescriptor(remoteTaskType, taskStarted, decorated), None,
      parent)
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate] = {
    Some(StatusUpdate(taskFinished.value, Some(event.eventPayload)))
  }

}