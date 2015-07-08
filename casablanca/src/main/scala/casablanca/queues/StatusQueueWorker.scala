package casablanca.queues

import java.util.Date
import casablanca.task.RelativeScheduledStatusUpdate
import casablanca.task.Task
import casablanca.task.TaskHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import _root_.sss.ancillary.Logging
import scala.annotation.tailrec

class StatusQueueWorker(queue: BlockingQueue[StatusQueue]) extends Logging {

  def start {
    (new Thread(runnable, s"StatusQueueWorker")).start
  }

  private val runnable = new Runnable {

    var quietCount = 0
    var loopCount = 0

    @tailrec
    override def run {
      try {
        val polled = queue.take
        try {

          val t = polled.poll
          if (t == null) {
            quietCount += 1
            if (quietCount > queue.size() * 2) {
              loopCount += 1
              if (loopCount == 1 || loopCount % 100 == 0) {
                log.info(s"Quiet Count is ${quietCount}, in quiet mode... ")
              }
              //This just prevents busy threads when the system is silent...
              // There's a better way of doing this. but other priorities take precedence. 
              Thread.sleep(50)
              quietCount = 0
            }
          } else {
            quietCount = 0
            polled.run(t)
          }

        } catch {
          case e: Exception => {
            log.error("FATAL: A status queue has failed to process a message correctly. ", e)
          }
        } finally {
          queue.put(polled)
        }

      } catch {
        case e: Exception => {
          log.warn("StatusQueueWorker exiting ... ", e)
          throw e
        }
      }
      run
    }

  }
}