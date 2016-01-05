package sss.casablanca.webservice.remotetasks

import java.util.UUID

import sss.casablanca.task.{ BaseTaskHandlerFactory, Task, TaskDescriptor, TaskHandler, TaskHandlerContext, TaskParent, TaskStatus }

object RemoteTaskHandlerFactory extends BaseTaskHandlerFactory {

  import sss.casablanca.webservice.remotetasks.RemoteTaskJsonMapper._

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