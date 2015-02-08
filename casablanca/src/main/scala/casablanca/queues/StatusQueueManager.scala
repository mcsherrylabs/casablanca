package casablanca.queues

import casablanca.task.TaskManager
import casablanca.task.Task
import casablanca.task.TaskUpdate
import casablanca.handler.StatusHandlerFactory
import casablanca.handler.HandlerUpdate
import casablanca.handler.ScheduledStatusUpdate
import casablanca.handler.StatusUpdate


class StatusQueueManager( tm: TaskManager, statusHandlerFactory: StatusHandlerFactory) {

  val statusQueueMap: Map[Int, StatusQueue] = {    
    statusHandlerFactory.getSupportedStatuses.map( s => s -> new StatusQueue(s,this)).toMap
  }
  
  val statusQueues = statusQueueMap.values

  def getHandler(status:Int) = statusHandlerFactory.getHandler(status)
  
  // Ignore result val here.
  def pushTask(task:Task , handlerResult: HandlerUpdate) {
    
    handlerResult match {
      case StatusUpdate(nextStatus, attemptCount) => {
        
        
        if(task.status == nextStatus && attemptCount == 0) {
          val msg = s"Will not create busy loop for task ${task.id} by pushing same status ${task.status}"
          println(msg)
          throw new Error(msg)
        }
        
        val taskUpdate = TaskUpdate(nextStatus, None, attemptCount)
        try {
	        val t = tm.updateTaskStatus(task.id, taskUpdate)
	        println(s"Pushed task ${t}")
	        statusQueueMap.get(nextStatus).map( _.push(t))        
        } catch {
          case e: Exception => println(e.toString)
        }
      }
      
      case ScheduledStatusUpdate(nextStatus, schedule) => {
        
        val taskUpdate = if(task.status == nextStatus) {
          TaskUpdate(nextStatus, Some(schedule), task.attemptCount)
        } else {
          TaskUpdate(nextStatus, Some(schedule), 0)
        }
        tm.updateTaskStatus(task.id, taskUpdate)
      }       
    }
    
  }
  
  def findTasks(status: Int) : List[Task] = {
    tm.findTasks("taskType", status, None)
  }
  
  
  def attemptTask(task: Task): Task = {
    // inc attempts
    val taskUpdate = TaskUpdate(task.status, None, task.attemptCount + 1) 
    tm.updateTaskStatus(task.id, taskUpdate)
             
  }
}