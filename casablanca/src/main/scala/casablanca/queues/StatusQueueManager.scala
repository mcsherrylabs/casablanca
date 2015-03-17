package casablanca.queues

import casablanca.task.TaskManager
import casablanca.task.Task
import casablanca.task.TaskUpdate
import casablanca.task.TaskHandlerFactory
import casablanca.task.HandlerUpdate
import casablanca.task.ScheduledStatusUpdate
import casablanca.task.StatusUpdate
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.task.TaskHandlerContext
import java.util.Date
import casablanca.task.TaskDescriptor
import casablanca.task.TaskSchedule
import casablanca.task.TaskParent
import casablanca.task.TaskStatus
import casablanca.util.Logging
import casablanca.util.ProgrammingError
import casablanca.util.Configure
import casablanca.util.ProgrammingError
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.TaskEvent
import casablanca.webservice.remotetasks.NodeConfig
import casablanca.webservice.TaskCompletionListener

class StatusQueueManager(tm: TaskManager,
  taskHandlerFactoryFactory: TaskHandlerFactoryFactory,
  aNodeConfig: NodeConfig,
  aTaskCompletionListener: TaskCompletionListener) extends Logging with Configure {

  lazy val puntIntoFutureMinutes = config.getInt("queueOverloadRescheduleMinutes")

  lazy val taskContext: TaskHandlerContext = new TaskHandlerContext {

    lazy val nodeConfig: NodeConfig = aNodeConfig
    lazy val taskCompletionListener = aTaskCompletionListener

    override def create(descriptor: TaskDescriptor,
      schedule: Option[TaskSchedule] = None,
      parent: Option[TaskParent] = None): Task = tm.create(descriptor, schedule, parent)

    override def startTask(descriptor: TaskDescriptor,
      schedule: Option[TaskSchedule] = None,
      parent: Option[TaskParent] = None) = {
      val t = create(descriptor, schedule, parent)
      StatusQueueManager.this.pushTask(t)
      t
    }

    override def handleEvent(taskId: String, event: TaskEvent) {
      val task = tm.getTask(taskId)
      taskHandlerFactoryFactory.getTaskFactory[TaskHandlerFactory](task.taskType) match {
        case None => throw new RuntimeException(s"No such task type factory ${task.taskType}")
        case Some(factory: TaskHandlerFactory) => factory.handleEvent(this, task, event)
      }
    }

    override def findChildren(parentTaskId: String, taskType: Option[String] = None): List[Task] = {
      tm.findChildren(parentTaskId, taskType)
    }

    override def pushTask(task: Task) = StatusQueueManager.this.pushTask(task)

    override def pushTask(task: Task, update: HandlerUpdate) {
      StatusQueueManager.this.pushTask(task, update)
    }

    override def getTask(taskId: String): Task = {
      tm.getTask(taskId)
    }
  }

  val statusQueueMap: Map[String, Map[Int, StatusQueue]] = {

    taskHandlerFactoryFactory.supportedFactories.map(f => {
      val taskType = f.getTaskType
      val statusMap: Map[Int, StatusQueue] = {
        f.getSupportedStatuses.map(s =>
          s.value -> new StatusQueue(taskContext, f.getStatusConfig(s.value), s.value, taskType, this)).toMap

      }
      (taskType -> statusMap)
    }).toMap

  }

  val statusQueues = statusQueueMap.values.flatMap(_.values)

  def getHandler(taskType: String, status: Int) = taskHandlerFactoryFactory.getHandler(taskType, TaskStatus(status))

  def pushTask(task: Task) {
    statusQueueMap.get(task.taskType).map(_.get(task.status).map(_.push(task)).map {
      pushed =>
        if (!pushed) {
          log.warn(s"StatusQueue refused our task !!, punting ${task}")
          pushTask(task, RelativeScheduledStatusUpdate(task.status, puntIntoFutureMinutes))
        }
    })
  }

  def pushTask(task: Task, handlerResult: HandlerUpdate) {

    val newStatus = handlerResult.nextStatus match {
      case Some(newStatus) => newStatus // we are updating the status
      case None => task.status
    }

    val newPayload = handlerResult.newStringPayload match {
      case Some(newStringPayload) => newStringPayload // we are updating the payload
      case None => task.strPayload
    }

    val newSchedule = handlerResult.scheduleAfter match {
      case Some(newSchedule) => newSchedule // we are updating the status
      case None => task.schedule
    }

    if (newStatus == task.status && task.schedule.isEmpty && handlerResult.scheduleAfter.isEmpty) {
      throw new ProgrammingError(s"Cannot push a task (${task}) with no update! ${handlerResult}")
    }

    // Reset the attempt count if this is a new status. 
    val updatedAttemptCount = if (newStatus == task.status) task.attemptCount else 0

    val taskUpdate = TaskUpdate(newStatus, newPayload, newSchedule, updatedAttemptCount)

    val updatedTask = tm.updateTaskStatus(task.id, taskUpdate)
    if (updatedTask.schedule.isEmpty) pushTask(updatedTask)

  }

  def findTasks(taskType: String, status: Int): List[Task] = tm.findTasks(taskType, Some(status))

  def attemptTask(task: Task): Task = {
    // inc attempts
    val taskUpdate = TaskUpdate(task.status, task.strPayload, task.schedule, task.attemptCount + 1)
    tm.updateTaskStatus(task.id, taskUpdate)

  }
}