package casablanca.queues

import casablanca.task.TaskManager
import casablanca.task.Task
import casablanca.handler.StatusHandlerFactory
import casablanca.task.StatusUpdate

class StatusQueueManager(statusHandlerFactory: StatusHandlerFactory, tm: TaskManager) {

  val statusQueueMap: Map[Int, StatusQueue] = {    
    statusHandlerFactory.getSupportedStatuses.map( s => s -> new StatusQueue(s,this)).toMap
  }
  
  val statusQueues = statusQueueMap.values

  def getHandler(status:Int) = statusHandlerFactory.getHandler(status)
  
  def pushTask(task:Task , handlerResult: StatusUpdate) {
    if(task.status != handlerResult.nextStatus) {
    	statusQueueMap.get(handlerResult.nextStatus).map( _.push(tm.updateTaskStatus(task.id, handlerResult)))
    } else {
    	statusQueueMap.get(handlerResult.nextStatus).map( _.push(task))  
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