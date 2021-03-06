package sss.casablanca.util

import java.util.concurrent.{ ScheduledExecutorService, TimeUnit }

import sss.ancillary.Logging

import scala.concurrent.{ Future, Promise }
import scala.util.Success

class TimeoutFuture[T](initialVal: T, timeout: Int)(implicit scheduleService: ScheduledExecutorService) extends Logging {

  private val p: Promise[T] = Promise[T]()

  private val runnable = new Runnable {
    def run {
      try {
        p.tryComplete(Success(initialVal))
      } catch {
        case e: Exception => log.error(s"Failed to update promise", e)
      }
    }
  }

  private val sc = scheduleService.schedule(runnable, timeout, TimeUnit.MILLISECONDS)

  def complete(result: T) {
    sc.cancel(true)
    p.tryComplete(Success(result))
  }

  def get: Future[T] = {
    p.future
  }
}