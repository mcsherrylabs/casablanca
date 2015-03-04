package casablanca.queues

import java.util.Date
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.Task
import casablanca.task.TaskHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import casablanca.util.Logging


class StatusQueueWorker(queue : BlockingQueue[StatusQueue]) extends Logging {
  
  
  def start {
    (new Thread(runnable, s"Worker ")).start
  }
  
  private val runnable = new Runnable {
  
    override def run {
	      val polled = queue.take
	      try {
		      val t = polled.poll
		      polled.run(t)
      	  } catch {
		        case e:Exception => {
		          log.error("FATAL: A status queue has failed to process a message correctly. ", e)
		        }
		  } finally {
		    queue.put(polled)
		  }
      
      run
    }
    	
  }
}