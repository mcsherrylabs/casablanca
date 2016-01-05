package sss.casablanca.queues

import java.util.Date
import java.util.concurrent.{ ScheduledExecutorService, TimeUnit }

import _root_.sss.ancillary.Logging
import sss.casablanca.task.{ HandlerUpdate, TaskManager }

class Scheduler(tm: TaskManager,
    scheduledExecutorService: ScheduledExecutorService,
    sqm: StatusQueueManager,
    scheduleIntervalInSeconds: Int) extends Logging {

  private val runnable = new Runnable {
    def run {
      try {
        reschedule()
      } catch {
        case e: Exception => log.error(s"Scheduler failed to reschedule tasks", e)
      }
    }
  }

  def start {
    scheduledExecutorService.scheduleAtFixedRate(runnable, scheduleIntervalInSeconds, scheduleIntervalInSeconds, TimeUnit.SECONDS)
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