package casablanca.task

import java.util.Date
import casablanca.webservice.remotetasks.NodeConfig

case class TaskParent(taskId: String, node: Option[String] = None)
case class TaskDescriptor(taskType: String, status: TaskStatus, strPayload: String)
case class TaskSchedule(when: Date)

trait TaskHandlerContext extends CreateTask with TaskStatuses {

  val nodeConfig: NodeConfig
  
  def startTask(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task

  def handleEvent(taskId: String, ev: TaskEvent)
  def pushTask(task: Task, update: HandlerUpdate)
  
  def getTask(taskId: String): Task

}