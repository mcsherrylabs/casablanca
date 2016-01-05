package sss.casablanca.queues

import java.util.Date
import java.util.concurrent.{ ScheduledExecutorService, TimeUnit }

import _root_.sss.ancillary.Logging
import sss.casablanca.task.{ SystemTaskStatuses, TaskManager }

class Reaper(tm: TaskManager,
    scheduledExecutorService: ScheduledExecutorService,
    scheduleIntervalInSeconds: Int,
    waitBeforeDeletingMinutes: Int) extends Logging with SystemTaskStatuses {

  private val waitBeforeDeletingMs = waitBeforeDeletingMinutes * 60 * 1000

  private val runnable = new Runnable {

    def run {
      try {
        val now = new Date()
        val beforeWhen = new Date(now.getTime() - waitBeforeDeletingMs)
        val deleted = tm.deleteTasks(systemFinished.value, beforeWhen)
        log.info(s"Reaper deleted ${deleted} 'system finished' tasks ... ")
      } catch {
        case e: Exception => log.error(s"Scheduler failed to reschedule tasks", e)
      }
    }
  }

  def start {
    scheduledExecutorService.scheduleAtFixedRate(runnable,
      scheduleIntervalInSeconds,
      scheduleIntervalInSeconds,
      TimeUnit.SECONDS)
  }

}