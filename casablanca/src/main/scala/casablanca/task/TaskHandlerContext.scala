package casablanca.task

import java.util.Date


case class TaskParent(taskId: String, node: Option[String] = None)
case class TaskDescriptor(taskType: String, status: Int, strPayload: String) extends TaskStatus
case class TaskSchedule(when: Date)

trait TaskHandlerContext extends CreateTask with TaskStatus {

  def startTask(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task

  def handleEvent(taskId: String, ev: String)
  def pushTask(task: Task, update: StatusUpdate)

}