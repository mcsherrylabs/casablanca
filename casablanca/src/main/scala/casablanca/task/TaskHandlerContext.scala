package casablanca.task

import java.util.Date
import casablanca.webservice.remotetasks.NodeConfig
import casablanca.webservice.TaskCompletionListener
import java.util.UUID

case class TaskParent(taskId: String, node: Option[String] = None)
case class TaskDescriptor(taskType: String, status: TaskStatus, strPayload: String, taskId: Option[String] = None)
case class TaskSchedule(when: Date)

trait TaskHandlerContext extends CreateTask with TaskStatuses {

  val nodeConfig: NodeConfig
  val taskCompletionListener: TaskCompletionListener

  def startTask(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task

  def findChildren(parentTaskId: String, taskType: Option[String] = None): List[Task]
  def handleEvent(taskId: String, ev: TaskEvent)
  def pushTask(task: Task, update: HandlerUpdate)
  def pushTask(task: Task)

  def getTask(taskId: String): Task
  def findTask(taskId: String): Option[Task]
  def generateNewTaskId: String = UUID.randomUUID().toString

}