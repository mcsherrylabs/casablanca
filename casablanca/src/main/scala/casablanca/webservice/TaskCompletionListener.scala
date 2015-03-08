package casablanca.webservice

import casablanca.task.Task
import scala.concurrent.Future
import scala.collection.concurrent.Map
import casablanca.util.TimeoutFuture
import scala.collection.concurrent.TrieMap
import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.ExecutionContext
import casablanca.util.Logging
import com.twitter.util.{Future => TwitFuture, Promise => TwitPromise }
import scala.util.Success
import scala.util.Failure

class TaskCompletionListener(implicit scheduleService: ScheduledExecutorService) extends Logging {

  import scala.concurrent.ExecutionContext.Implicits.global
  
  private val monitoredTasks: Map[String, TimeoutFuture[Task]] = new TrieMap()
  
  def listenForCompletion[T](defaultResult:Task, mapOnCompletion: Task =>  T,
      mininmumWaitTimeMs: Option[Int] = None): TwitFuture[T] = {
    log.debug(s"Pool size is now ${monitoredTasks.size}")
    
    mininmumWaitTimeMs match {
      case Some(timeOut) if timeOut > 0 => {
        val tf = new TimeoutFuture(defaultResult, mininmumWaitTimeMs.get)
	    monitoredTasks.put(defaultResult.id, tf)
	    val twitProm = TwitPromise[T]()
	    val f = tf.get.map(mapOnCompletion)
	    f.onComplete {
          case Success(t) => {
            monitoredTasks.remove(defaultResult.id)
            twitProm.setValue(t)
          }
          
          case Failure(e) => {
            log.warn(s"Failed to complete ", e)
            monitoredTasks.remove(defaultResult.id)
            twitProm.setValue(mapOnCompletion(defaultResult))
          }
	      
	    }
	    
        twitProm
      } 
      case x => TwitFuture.value(mapOnCompletion(defaultResult))
    }
     
  } 
  
  def complete(completedTask: Task) {
    monitoredTasks.get(completedTask.id) map { tf =>
      tf.complete(completedTask)
      //monitoredTasks.remove(completedTask.id)
    }
  }
}