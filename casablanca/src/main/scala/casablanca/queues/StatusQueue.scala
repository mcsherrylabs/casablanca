package casablanca.queues

import casablanca.task.Task
import java.util.concurrent.TimeUnit
import casablanca.task.RelativeScheduledStatusUpdate

class StatusQueue(status: Int, val taskType: String, statusQueueManager: StatusQueueManager) {

  private val maxRetries = 5
  private val statusHandler = statusQueueManager.getHandler(taskType , status).getOrElse( throw new Error(s"No handler exists for status ${status}"))
  private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](100)
  
    
    def init {
      statusQueueManager.findTasks(taskType, status).foreach(t => queue.add(t))
      //new StatusQueueWorker(status, this, statusQueueManager).start
    }
    
    def push(t: Task) {
      // check status?
      if(!queue.offer(t, 10000, TimeUnit.MILLISECONDS)) println(s"StatusQueue refused our task !!, dropped ${t}") 
    }
  
    def poll: Task = {
      val t = queue.poll(20000, TimeUnit.MILLISECONDS)
      if(t != null) statusQueueManager.attemptTask(t)      
      else t
    }
    
    def run(t: Task) {
    	    	
    	if(t != null) {
	      println(s"StatusQueueWorker ${status} on task ${t.id}")
	      try {
	        
	          if(t.attemptCount > 1) {
	            if(t.attemptCount <= maxRetries) {
	              val handlerResult = statusHandler.reTry(t)
	              statusQueueManager.pushTask(t, handlerResult)
	            } else println(s"Giving up on task ${t}, max try count exceeded (${maxRetries})")
	          } else {
	            val handlerResult = statusHandler.handle(t)
	            statusQueueManager.pushTask(t, handlerResult)
	          }
	          
	      } catch {
	        case ex : Exception => {
	          println("Puked handling task, retry in 0 minutes")
	          statusQueueManager.pushTask(t, RelativeScheduledStatusUpdate(t.status, 0))
	        }
	      }
	      
	    } else {
	      println(s"StatusQueueWorker status=${status} going around again...")
	    }
	    
     }
}

