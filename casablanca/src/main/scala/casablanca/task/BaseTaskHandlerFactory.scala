package casablanca.task

import java.util.LinkedHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit

trait BaseTaskHandlerFactory extends TaskHandlerFactory {

  private def lruCache(maxSize: Int) = {
    new LinkedHashMap[String, Lock](maxSize * 4 / 3, 0.75f, true) {
      override def removeEldestEntry(eldest: java.util.Map.Entry[String, Lock]): Boolean = {
        size() > maxSize
      }
    }
  }

  private val taskLockCache = lruCache(12)
  
  def getSupportedStatuses: Set[Int]
  def getHandler[T >: TaskHandler](status: Int): Option[T]

  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: String) {
    val l: Lock = taskLockCache.synchronized {
      if (!taskLockCache.containsKey(task.id)) {
        taskLockCache.put(task.id, new ReentrantLock)
      }
      taskLockCache.get(task.id)
    }
    try {
      if (l.tryLock(1000, TimeUnit.MILLISECONDS)) {
        println("GOING TO CONSUME")
        consume(taskContext, task, ev) map { up => taskContext.pushTask(task, up) }
      } else throw new RuntimeException(s"Could not lock task id ${task.id}")
    } finally l.unlock
  }

  def consume(taskContext: TaskHandlerContext, task: Task, event: String): Option[StatusUpdate] = {
    throw new UnsupportedOperationException
  }

}

