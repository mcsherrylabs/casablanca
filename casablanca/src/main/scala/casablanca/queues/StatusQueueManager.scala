package casablanca.queues

import casablanca.task.TaskManager
import casablanca.task.Task
import casablanca.task.TaskUpdate
import casablanca.task.TaskHandlerFactory
import casablanca.task.HandlerUpdate
import casablanca.task.ScheduledStatusUpdate
import casablanca.task.StatusUpdate
import casablanca.task.TaskHandlerFactoryFactory

class StatusQueueManager( tm: TaskManager, taskHandlerFactoryFactory: TaskHandlerFactoryFactory) {

  val statusQueueMap: Map[String, Map[Int, StatusQueue]] = {    
    
    taskHandlerFactoryFactory.getSupportedFactories.map( f => {
      val taskType = f.getTaskType
      val statusMap: Map[Int, StatusQueue] = {
        f.getSupportedStatuses.map( s => 
        	s -> new StatusQueue(s,taskType, this)).toMap
        
      }
      (taskType -> statusMap)
    }).toMap
    
  }
  
  val statusQueues = statusQueueMap.values.flatMap(_.values)

  def getHandler(taskType: String, status:Int) = taskHandlerFactoryFactory.getHandler(taskType, status)
  
  def createTask(taskType: String, initialStatus: Int, strPayload: String, intPayload: Int): Task  = {
    tm.create(taskType, initialStatus, strPayload, intPayload)
  }
  
  def pushTask(task:Task) {    
    statusQueueMap.get(task.taskType).map( _.get(task.status).map( _.push(task)))    
  }
  
  def pushTask(task:Task , handlerResult: HandlerUpdate) {
    
    handlerResult match {
      case StatusUpdate(nextStatus, newStringPayload, newIntValue, attemptCount) => {
        
        if(task.status == nextStatus && attemptCount == 0) {
          val msg = s"Will not create busy loop for task ${task.id} by pushing same status ${task.status}"
          println(msg)
          throw new Error(msg)
        }
        
        val taskUpdate = TaskUpdate(nextStatus, newStringPayload, newIntValue, None, attemptCount)
        try {
	        val t = tm.updateTaskStatus(task.id, taskUpdate)
	        //println(s"Pushed task ${t}")
	        statusQueueMap.get(t.taskType).map(_.get(nextStatus).map( _.push(t)))        
        } catch {
          case e: Exception => println(e.toString)
        }
      }
      
      case ScheduledStatusUpdate(nextStatus, schedule, newStringPayload, newIntValue) => {
        
        val taskUpdate = if(task.status == nextStatus) {
          TaskUpdate(nextStatus, newStringPayload, newIntValue, Some(schedule), task.attemptCount)
        } else {
          TaskUpdate(nextStatus, newStringPayload, newIntValue, Some(schedule), 0)
        }
        tm.updateTaskStatus(task.id, taskUpdate)
      }       
    }
    
  }
  
  def findTasks(taskType: String, status: Int) : List[Task] = tm.findTasks(taskType, status)
  
  def attemptTask(task: Task): Task = {
    // inc attempts
    val taskUpdate = TaskUpdate(task.status, None, None, None, task.attemptCount + 1) 
    tm.updateTaskStatus(task.id, taskUpdate)
             
  }
}