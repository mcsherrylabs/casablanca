package casablanca.util

import java.util.concurrent.locks.Lock
import java.util.LinkedHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

class LockTimeoutException(msg: String) extends RuntimeException(msg)

trait Lockable[T] {

  private def lruCache(maxSize: Int) = {
    new LinkedHashMap[T, Lock](maxSize * 4 / 3, 0.75f, true) {
      override def removeEldestEntry(eldest: java.util.Map.Entry[T, Lock]): Boolean = {
        size() > maxSize
      }
    }
  }

  private val lockCache = lruCache(12)

  def doLocked[R](lockable: T, f: () => R, timeOutMillis: Int = 1000): R = {
    val l: Lock = lockCache.synchronized {
      if (!lockCache.containsKey(lockable)) {
        lockCache.put(lockable, new ReentrantLock)
      }
      lockCache.get(lockable)
    }
    try {
      if (l.tryLock(timeOutMillis, TimeUnit.MILLISECONDS)) {
        println("GOING TO CONSUME")
        f()

      } else throw new LockTimeoutException(s"Could not lock ${lockable} in time ${timeOutMillis} (ms)")
    } finally l.unlock
  }

}