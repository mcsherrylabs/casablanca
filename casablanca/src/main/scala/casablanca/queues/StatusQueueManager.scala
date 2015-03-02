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

class StatusQueueManager(tm: TaskManager, taskHandlerFactoryFactory: TaskHandlerFactoryFactory) {

  lazy val taskContext: TaskHandlerContext = new TaskHandlerContext {

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

    override def handleEvent(taskId: String, event: String) {
      println(s"Getting task ... " + taskId)
      val task = tm.getTask(taskId)
      println(s"Got task ... " + task)
      println(s"Getting task ... ")
      taskHandlerFactoryFactory.getTaskFactory[TaskHandlerFactory](task.taskType) match {
        case None => throw new RuntimeException(s"No such task type factory ${task.taskType}")
        case Some(factory: TaskHandlerFactory) => factory.handleEvent(this, task, event)
      }
    }

    override def pushTask(task: Task, update: StatusUpdate) {
      StatusQueueManager.this.pushTask(task, update)
    }
  }

  val statusQueueMap: Map[String, Map[Int, StatusQueue]] = {

    taskHandlerFactoryFactory.supportedFactories.map(f => {
      val taskType = f.getTaskType
      val statusMap: Map[Int, StatusQueue] = {
        f.getSupportedStatuses.map(s =>
          s -> new StatusQueue(taskContext, s, taskType, this)).toMap

      }
      (taskType -> statusMap)
    }).toMap

  }

  val statusQueues = statusQueueMap.values.flatMap(_.values)

  def getHandler(taskType: String, status: Int) = taskHandlerFactoryFactory.getHandler(taskType, status)

  def pushTask(task: Task) {
    statusQueueMap.get(task.taskType).map(_.get(task.status).map(_.push(task)))
  }

  def pushTask(task: Task, handlerResult: HandlerUpdate) {

    handlerResult match {
      case StatusUpdate(nextStatus, newStringPayload, attemptCount) => {

        if (task.status == nextStatus && attemptCount == 0) {
          val msg = s"Will not create busy loop for task ${task.id} by pushing same status ${task.status}"
          println(msg)
          throw new Error(msg)
        }

        val taskUpdate = TaskUpdate(nextStatus, newStringPayload, None, attemptCount)
        try {
          val t = tm.updateTaskStatus(task.id, taskUpdate)
          //println(s"Pushed task ${t}")
          statusQueueMap.get(t.taskType).map(_.get(nextStatus).map(_.push(t)))
        } catch {
          case e: Exception => println(e.toString)
        }
      }

      case ScheduledStatusUpdate(nextStatus, schedule, newStringPayload) => {

        val taskUpdate = if (task.status == nextStatus) {
          TaskUpdate(nextStatus, newStringPayload, Some(schedule), task.attemptCount)
        } else {
          TaskUpdate(nextStatus, newStringPayload, Some(schedule), 0)
        }
        tm.updateTaskStatus(task.id, taskUpdate)
      }
    }

  }

  def findTasks(taskType: String, status: Int): List[Task] = tm.findTasks(taskType, status)

  def attemptTask(task: Task): Task = {
    // inc attempts
    val taskUpdate = TaskUpdate(task.status, None, None, task.attemptCount + 1)
    tm.updateTaskStatus(task.id, taskUpdate)

  }
}