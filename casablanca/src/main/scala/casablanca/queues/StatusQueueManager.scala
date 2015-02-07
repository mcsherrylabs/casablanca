package casablanca.queues

import casablanca.task.TaskManager
import casablanca.task.Task
import casablanca.handler.StatusHandlerFactory

class StatusQueueManager(statusHandlerFactory: StatusHandlerFactory, tm: TaskManager) {

  val statusQueueMap: Map[Int, StatusQueue] = {    
    statusHandlerFactory.getSupportedStatuses.map( s => s -> new StatusQueue(s,this)).toMap
  }
  
  val statusQueues = statusQueueMap.values

  def getHandler(status:Int) = statusHandlerFactory.getHandler(status)
  
  def pushTask(task:Task , newStatus: Int) {
    if(task.status != newStatus) {
    	statusQueueMap.get(newStatus).map( _.push(tm.pushTask(task.id, newStatus)))
    } else {
    	statusQueueMap.get(newStatus).map( _.push(task))  
    }
    
  }
  
  def findTasks(status: Int) : List[Task] = {
    tm.findTasks("taskType", status, None)
  }
  
  
  def attemptTask(task: Task): Task = {
    // inc attempts
    tm.incAttempts(task)    
  }
}