package casablanca.queues

import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import casablanca.task.TaskManager
import casablanca.task.StatusUpdate


class Scheduler(tm: TaskManager, sqm: StatusQueueManager, scheduleIntervalInSeconds: Int) {

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
      t => {
        println(s"Scheduler found task ${t}")
        sqm.pushTask(t, StatusUpdate(t.status, None, None, t.attemptCount))
      }
    }
  }
  
}