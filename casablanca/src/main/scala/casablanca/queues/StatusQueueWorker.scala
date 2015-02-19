package casablanca.queues

import java.util.Date
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.Task
import casablanca.task.TaskHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit


class StatusQueueWorker(queue : BlockingQueue[StatusQueue]) {
  
  
  def start {
    (new Thread(runnable, s"Worker ")).start
  }
  
  private val runnable = new Runnable {
  
    override def run {
      
	      val polled = queue.poll(2000, TimeUnit.MILLISECONDS)
	      try {
	        if(polled != null) {
		      val t = polled.poll
		      polled.run(t)
	        }
      	  } catch {
		        case e:Exception => println(e)
		  } finally {
		        if(polled != null) queue.put(polled)
		  }
      
      run
    }
    	
  }
}