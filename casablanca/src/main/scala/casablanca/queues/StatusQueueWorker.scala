package casablanca.queues

import java.util.Date
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.Task
import casablanca.task.TaskHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import casablanca.util.Logging
import scala.annotation.tailrec


class StatusQueueWorker(queue : BlockingQueue[StatusQueue]) extends Logging {
  
  
  def start {
    (new Thread(runnable, s"StatusQueueWorker")).start
  }
  
  private val runnable = new Runnable {
  
    @tailrec
    override def run {
      try {
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
  		 
      } catch {
        case e : Exception => {
          log.warn("StatusQueueWorker exiting ... ", e)
          throw e
        }                
      }
      run  
    }
    	
  }
}