package casablanca.queues

import casablanca.task.Task
import java.util.concurrent.TimeUnit

class StatusQueue(status: Int, taskType: String, statusQueueManager: StatusQueueManager) {

    private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](100)
    
    def start {
      statusQueueManager.findTasks(taskType, status).foreach(t => queue.add(t))
      new StatusQueueWorker(status, this, statusQueueManager).start
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
}

