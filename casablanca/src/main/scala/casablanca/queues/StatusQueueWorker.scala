package casablanca.queues

import java.util.Date
import casablanca.handler.RelativeScheduledStatusUpdate
import casablanca.task.Task


class StatusQueueWorker[T >: Task](status: Int, queue : StatusQueue, 
    statusQueueManager: StatusQueueManager) extends Runnable {
  
  private val maxRetries = 5
  
  def start {
    (new Thread(this, s"Worker ${status}")).start
  }
  
  val statusHandler = statusQueueManager.getHandler(status).getOrElse( throw new Error(s"No handler exists for status ${status}"))
  
  override def run {
    
    val t = queue.poll
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
    run
  }
}