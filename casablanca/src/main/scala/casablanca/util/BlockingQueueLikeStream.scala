package casablanca.util

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * A trait to turn an infinite stream into the head of a blocking list
 * to provide ordered access to the stream elements.
 *
 * This was written to provide access to time dependent streams.
 * ie the stream could represent a database table that is being written to over time
 * so the stream might look like
 * None, Some(row), Some(row), None, None, Some(row)
 * ...depending on when the table is written to...
 *
 */
trait BlockingQueueLikeStream[A] extends Logging {

  private val l = new ReentrantLock

  private var stream: Stream[Option[A]] = initialiseStream

  /**
   * Note this will NOT wait timeOut for a Some() element to be added to the
   * queue....as None is a valid element
   */
  def poll(timeOut: Long = Long.MaxValue, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Option[A] = {
    if (l.tryLock(timeOut, timeUnit)) {
      try {
        if (stream.isEmpty) None
        else {
          val res = stream.head
          stream = stream.tail
          res
        }
      } catch {
        case e: Exception => log.error("Queue Like Stream failed to process tail! ", e); throw e
      } finally l.unlock
    } else None
  }

  def initialiseStream: Stream[Option[A]]
}