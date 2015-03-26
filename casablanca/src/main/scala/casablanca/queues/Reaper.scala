package casablanca.queues

import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import casablanca.task.TaskManager
import casablanca.task.StatusUpdate
import casablanca.task.HandlerUpdate
import casablanca.util.Logging
import casablanca.task.SystemTaskStatuses
import java.util.concurrent.ScheduledExecutorService

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