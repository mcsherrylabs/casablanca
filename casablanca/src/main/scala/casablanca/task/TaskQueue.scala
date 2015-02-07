package casablanca.task

import casablanca.db.Table
import java.util.concurrent.TimeUnit
import collection.JavaConversions._
import java.util.Date

trait TaskQueue {

  def poll(timeout: Int, consumerIdentifier: String, retryCount: Int = 3): Option[Task]
  def push(t: Task, newStatus: Int) 
}

class TaskQueueImpl(taskManager:TaskManager, taskType: String, status: Int, queueSize: Int) extends TaskQueue {
  
  private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](queueSize)
  private var latestTaskTimestamp : Option[Date] = None 
  
  override def push(t: Task, newStatus: Int) {
	  taskManager.pushTask(t.id, newStatus)
  }
  
  override def poll(timeout: Int, consumerIdentifier: String, retryCount: Int): Option[Task] = {
    synchronized  {
        // peek at the head of the queue,
        // only get it if the update works.
        
        val task = queue.poll(timeout, TimeUnit.MILLISECONDS)
    
	    if(task == null) {
	      val listTasks = taskManager.findTasks(taskType, status, latestTaskTimestamp)
	      if(listTasks.size > 0) {
	          latestTaskTimestamp = Some(listTasks(0).createTime) 	    	  
		      queue.addAll(listTasks.reverse)
		      poll(timeout, consumerIdentifier)
	      } else None 
	    } else {
	       try { 
	         //taskManager.claimTask(task.id, consumerIdentifier)
	         Some(task)
	       } catch {
	       	case e: Exception => {
	       	  // log it
	       	  queue.put(task)
	       	  poll(timeout, consumerIdentifier, retryCount - 1)
	       	}
	       
	       }
	    }
    }
  }
}