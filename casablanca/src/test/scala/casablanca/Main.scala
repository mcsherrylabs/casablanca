package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager

object Main {

  def main(args: Array[String]): Unit = {
   
    val tm = new TaskManager("taskManager")
    val sqm = new GenericStatusHandlerFactory
    val statusQManager = new StatusQueueManager(sqm, tm) 
    
    val statusQueues = statusQManager.statusQueues
    val threads= statusQueues.map { q => 
    	new Thread(new Runnable {
    		def run() {
    			println("Thread started")
    			q.init
    		}
    	})    
    }
   
   tm.create("taskType", 0, "strPayload", 33)
   tm.create("taskType", 2, "strPayload2", 33)
   tm.create("taskType", 2, "strPayload2", 33)
   tm.create("taskType", 2, "strPayload2", 33)
   tm.create("taskType", 2, "strPayload2", 33)
   
    threads.foreach { t => t.start }
    
  }

}