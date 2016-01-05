package sss.casablanca.queues

import sss.casablanca.task.{ BaseTaskHandlerFactory, HandlerUpdate, StatusUpdate, Task, TaskHandler, TaskHandlerContext, TaskStatus }

class GenericStatusHandler(val status: Int) extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    println(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")
    Thread.sleep(1000)
    if (task.status == 4 && task.attemptCount < 3) {
      println("Throwing!")
      throw new RuntimeException("Whas goin on?")
    }
    StatusUpdate(status + 1)
  }

  override def reTry(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    println(s"RETRY ${task.id} status ${task.status}, count ${task.attemptCount}  ")
    handle(taskContext, task)
  }
}

object GenericStatusHandlerFactory extends BaseTaskHandlerFactory {

  def getTaskType: String = "genericTask"

  override def getSupportedStatuses: Set[TaskStatus] = (0 to 9).map(TaskStatus(_)).toSet

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[TaskHandler] = {
    if (status.value < 10) Some(new GenericStatusHandler(status.value))
    else None
  }
}

