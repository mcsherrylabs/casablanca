package casablanca.task

import java.util.concurrent.TimeUnit
import java.util.concurrent.BlockingQueue
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

class StatusQConsumer(taskQueue: TaskQueue, consumerIdentifier: String, consumerQueueSize: Int, numWorkers: Int) {

  private val queue = new java.util.concurrent.ArrayBlockingQueue[Task](consumerQueueSize)
  
  @volatile private var stopFlag = false
  
  def stop = { stopFlag = true } 
  
  new Consumerworker(queue, taskQueue, consumerIdentifier).start
  
  /*for (i <- 0 to numWorkers) {
      future {
         
      }
    }8*/
  
  def start {
    future {
    while(true) {
      taskQueue.poll(600, consumerIdentifier) map { t =>
          println(s"Got task ${t.id}, offering to workers")
    	  val res = queue.put(t)
      }
    }
    }
  }
}

class Consumerworker(queue: BlockingQueue[Task], taskQueue: TaskQueue, consumerIdentifier: String) {
  
  def start {
    future {
    while(true) {
     
    val t = queue.take()
    println(s"Got task ${t.id}, pushing ... ${t.status + 1}")
    taskQueue.push(t, t.status + 1)
    }
    }
  }
  
}