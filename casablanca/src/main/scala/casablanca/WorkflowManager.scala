package casablanca

import casablanca.task.Task
import casablanca.handler.HandlerUpdate
import casablanca.handler.StatusHandler
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.TaskManager
import casablanca.handler.StatusHandlerFactory
import java.util.Date

/**
 * Facade 
 */
trait WorkflowManager {

  def getHandler(status:Int): Option[StatusHandler]
  def createTask(taskType: String, initialStatus: Int, strPayload: String, intPayload: Int, schedule: Option[Date] = None): Task 
  def pushTask(task:Task)
  def pushTask(task:Task , handlerResult: HandlerUpdate)
  def findTasks(taskType: String, status: Int) : List[Task]
}


class WorkflowManagerImpl(tm: TaskManager,
    statusQManager: StatusQueueManager,
    statusHandlerFactory: StatusHandlerFactory, 
    scheduler: Scheduler) extends WorkflowManager {
  
  def stop {
    // todo add other stops for threads
    tm.close
  }
  
  def start {
    
    val statusQueues = statusQManager.statusQueues
    statusQueues.map ( q => q.start)
    
    scheduler.start
  }
  
  def getHandler(status:Int): Option[StatusHandler] = statusHandlerFactory.getHandler(status)    
  
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