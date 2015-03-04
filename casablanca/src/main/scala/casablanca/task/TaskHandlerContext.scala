package casablanca.task

import java.util.Date

case class TaskParent(taskId: String, node: Option[String] = None)
case class TaskDescriptor(taskType: String, status: TaskStatus, strPayload: String)
case class TaskSchedule(when: Date)

trait TaskHandlerContext extends CreateTask with TaskStatuses {

  def startTask(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task

  def handleEvent(taskId: String, ev: String)
  def pushTask(task: Task, update: HandlerUpdate)

}