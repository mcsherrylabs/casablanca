package casablanca.task

import java.util.Date
import casablanca.webservice.remotetasks.NodeConfig
import casablanca.webservice.TaskCompletionListener

case class TaskParent(taskId: String, node: Option[String] = None)
case class TaskDescriptor(taskType: String, status: TaskStatus, strPayload: String)
case class TaskSchedule(when: Date)

trait TaskHandlerContext extends CreateTask with TaskStatuses {

  val nodeConfig: NodeConfig
  val taskCompletionListener: TaskCompletionListener
  
  def startTask(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task

  def handleEvent(taskId: String, ev: TaskEvent)
  def pushTask(task: Task, update: HandlerUpdate)
  def pushTask(task: Task)
  
  def getTask(taskId: String): Task

}