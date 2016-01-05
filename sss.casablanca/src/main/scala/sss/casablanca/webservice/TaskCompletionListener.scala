package sss.casablanca.webservice

import java.util.concurrent.ScheduledExecutorService

import _root_.sss.ancillary.Logging
import com.twitter.util.{ Future => TwitFuture, Promise => TwitPromise }
import sss.casablanca.task.Task
import sss.casablanca.util.TimeoutFuture

import scala.collection.concurrent.{ Map, TrieMap }
import scala.util.{ Failure, Success }

class TaskCompletionListener(implicit scheduleService: ScheduledExecutorService) extends Logging {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val monitoredTasks: Map[String, TimeoutFuture[Task]] = new TrieMap()

  def listenForCompletion[T](defaultResult: Task, mapOnCompletion: Task => T,
    mininmumWaitTimeMs: Option[Int] = None): TwitFuture[T] = {

    log.debug(s"Pool size is now ${monitoredTasks.size}")

    mininmumWaitTimeMs match {
      case Some(timeOut) if timeOut > 0 => {

        val tf = new TimeoutFuture(defaultResult, timeOut)
        monitoredTasks.put(defaultResult.id, tf)

        val twitProm = TwitPromise[T]()

        tf.get.map(mapOnCompletion).onComplete {

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
    }
  }
}