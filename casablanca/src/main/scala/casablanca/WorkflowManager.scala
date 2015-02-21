package casablanca

import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.TaskHandler
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.TaskManager
import casablanca.task.TaskHandlerFactory
import java.util.Date
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueue
import casablanca.queues.StatusQueueWorker

/**
 * Facade 
 */
trait WorkflowManager {

  def start 
  def stop 
  /*def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T]
  def getHandler(taskType: String, status:Int): Option[TaskHandler]
  def createTask(taskType: String, initialStatus: Int, strPayload: String, intPayload: Int, schedule: Option[Date] = None): Task 
  def pushTask(task:Task)
  def pushTask(task:Task , handlerResult: HandlerUpdate)
  def findTasks(taskType: String, status: Int) : List[Task]*/
}


class WorkflowManagerImpl(tm: TaskManager,
    statusQManager: StatusQueueManager,
    statusHandlerFactory: TaskHandlerFactoryFactory, 
    scheduler: Scheduler) extends WorkflowManager {
  
  def stop {
    // todo add other stops for threads
    tm.close
  }
  
  def start {
    
    val statusQueues = statusQManager.statusQueues
    statusQueues.map ( q => q.init)
    val workerQueue = new java.util.concurrent.ArrayBlockingQueue[StatusQueue](statusQueues.size)
    statusQueues.foreach { e => workerQueue.put(e)}
    for(i <- 0 to 5) {
      new StatusQueueWorker(workerQueue).start
    }
    scheduler.start
  }
  
  def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T] = {
    statusHandlerFactory.getTaskFactory[T](taskType)
  }
  
  def getHandler(taskType: String, status:Int): Option[TaskHandler] = statusHandlerFactory.getHandler(taskType, status)    
  
  def createTask(taskType: String, 
      initialStatus: Int, 
      strPayload: String = "", 
      intPayload: Int = 0, 
      scheduleTime: Option[Date] = None): Task = {
    
	  tm.create(taskType, initialStatus, strPayload, intPayload, scheduleTime)
  } 
  
  
  def pushTask(task:Task) = statusQManager.pushTask(task)
  
  def pushTask(task:Task , handlerResult: HandlerUpdate) = statusQManager.pushTask(task, handlerResult) 
  
  def findTasks(taskType: String, status: Int) : List[Task] = tm.findTasks(taskType, status)
} 