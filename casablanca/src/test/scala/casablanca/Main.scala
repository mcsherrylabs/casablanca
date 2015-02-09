package casablanca

import casablanca.task.TaskManager
import casablanca.queues.GenericStatusHandlerFactory
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.handler.StatusUpdate

object Main {

  def main(args: Array[String]): Unit = {
   
    val tm = new TaskManager("taskManager")
    val shf = new GenericStatusHandlerFactory
    val statusQManager = new StatusQueueManager(tm, shf)
    
    val scheduler = new Scheduler(tm, statusQManager, 10)
    
    scheduler.start
    
    val statusQueues = statusQManager.statusQueues
    val threads = statusQueues.map ( q => q.start)    	
   
    val t = statusQManager.createTask("taskType", 0, "strPayload", 33)
    statusQManager.pushTask(t)
    
//   tm.create("taskType", 2, "strPayload2", 33)
//   tm.create("taskType", 2, "strPayload2", 33)
//   tm.create("taskType", 2, "strPayload2", 33)
//   tm.create("taskType", 2, "strPayload2", 33)
   
    
  }

}