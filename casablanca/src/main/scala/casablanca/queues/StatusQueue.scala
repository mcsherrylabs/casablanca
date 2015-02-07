package casablanca.queues

import casablanca.task.Task
import java.util.concurrent.TimeUnit

class StatusQueue(status: Int, statusQueueManager: StatusQueueManager) {

    private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](100)
    
    def init {
      statusQueueManager.findTasks(status).foreach(t => queue.add(t))
      new StatusQueueWorker(status, this, statusQueueManager).start
    }
    
    def push(t: Task) {
      // check status?
      if(!queue.offer(t, 10000, TimeUnit.MILLISECONDS)) println("StatusQueue refused our task !!") 
    }
  
    def poll: Task = {
      val t = queue.poll(10000, TimeUnit.MILLISECONDS)
      if(t != null) statusQueueManager.attemptTask(t)      
      else t
    }
}

class StatusQueueWorker(status: Int, queue : StatusQueue, 
    statusQueueManager: StatusQueueManager) {
  
  val statusHandler = statusQueueManager.getHandler(status).getOrElse( throw new Error(s"No handler exists for status ${status}"))
  
  def start {
    val t = queue.poll
    if(t != null) {
      println("StatusQueueWorker got a task !!")
      try {
          val newStatus = if(t.attemptCount > 1) {
        	  statusHandler.reTry(t)	    	      
          } else {
            statusHandler.handle(t)
          }
    	  statusQueueManager.pushTask(t, newStatus)
    	  
      } catch {
        case ex : Exception => {
          println("Puked handling task, retry.")
          statusQueueManager.pushTask(t, t.status)
        }
      }
      
    } else {
      println("StatusQueueWorker going around again...")
    }
    start
  }
}