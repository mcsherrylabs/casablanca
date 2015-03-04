package casablanca.queues

import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import casablanca.task.TaskManager
import casablanca.task.StatusUpdate
import casablanca.task.HandlerUpdate
import casablanca.util.Logging

class Scheduler(tm: TaskManager, sqm: StatusQueueManager, scheduleIntervalInSeconds: Int) extends Logging {

  private val runnable = new Runnable {
    def run {
      reschedule()
      start
    }
  }

  def start {
    Executors.newScheduledThreadPool(1).schedule(runnable, scheduleIntervalInSeconds, TimeUnit.SECONDS)
  }

  def reschedule(beforeWhen: Date = new Date()) {
    tm.findScheduledTasks(beforeWhen) foreach {
      t =>
        {
          try {
            log.debug(s"Scheduler found task ${t}")
            sqm.pushTask(t, HandlerUpdate(None, None, Some(None)))
          } catch {
            case e: Exception => log.error(s"Scheduler failed to push task ${t}")
          }

        }
    }
  }

}