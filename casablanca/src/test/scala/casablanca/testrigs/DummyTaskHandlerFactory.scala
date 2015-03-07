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
    log.info(s"Consuming ${status} returning ${status + 1}, task.attemptCount is ${task.attemptCount}")
    if(status == 1009) HandlerUpdate.success  
    else StatusUpdate(status + 1)
  }

}

object DummyTaskHandlerFactory extends BaseTaskHandlerFactory {

  def getTaskType: String = "dummyTask"

  override def getSupportedStatuses: Set[TaskStatus] = Set(taskStarted) ++ (1000 to 1009).map(TaskStatus(_)).toSet ++ super.getSupportedStatuses

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[TaskHandler] = {
    if(status == taskStarted) Some(new DummyTaskHandler(1000))
    else if(status.value >= 1000 && status.value <= 1009) Some(new DummyTaskHandler(status.value))
    else super.getHandler(status)
  }
}

