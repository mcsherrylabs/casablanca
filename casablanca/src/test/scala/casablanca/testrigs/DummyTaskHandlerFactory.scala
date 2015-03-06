package casablanca.testrigs

import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.HandlerUpdate
import casablanca.task.StatusUpdate
import casablanca.task.TaskManager
import casablanca.task.TaskHandlerContext
import casablanca.task.TaskStatus

class DummyTaskHandler(val status: Int) extends TaskHandler {

  def handle(taskContext: TaskHandlerContext, task: Task): HandlerUpdate = {
    println(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")    
    StatusUpdate(status + 1)
  }

}

object DummyTaskHandlerFactory extends BaseTaskHandlerFactory {

  def getTaskType: String = "dummyTask"

  override def getSupportedStatuses: Set[TaskStatus] = (1000 to 1009).map(TaskStatus(_)).toSet

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[TaskHandler] = {
    Some(new DummyTaskHandler(status.value))    
  }
}

