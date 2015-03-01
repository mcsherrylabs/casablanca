package casablanca.task

import java.util.Date
import java.util.LinkedHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import java.util.concurrent.TimeUnit

trait TaskStatus {
  val systemFinished = 0
  val systemFailed = 1
  val systemSuccess = 2
  val systemStarted = 10

  val taskFinished = 100
  val taskStarted = 101
}

case class RemoteTask(node: String, taskType: String)

trait HandlerUpdate

trait TaskEvent {
  val taskId: String
}

trait TaskHandler extends TaskStatus {
  def handle(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate
  def reTry(taskHandlerContext: TaskHandlerContext, task: Task): HandlerUpdate = handle(taskHandlerContext, task)
}

trait TaskHandlerContext {
  def createTask(taskType: String, status: Int, strPayload: String = "",
    intPayload: Int = 0,
    parentTaskId: Option[String] = None,
    parentNode: Option[String] = None,
    scheduleTime: Option[Date] = None): Task

  def startTask(taskType: String, status: Int, strPayload: String = "", intPayload: Int = 0,
    parentTaskId: Option[String] = None,
    parentNode: Option[String] = None,
    scheduleTime: Option[Date] = None): Task

  def handleEvent(taskId: String, ev: String)
  def pushTask(task: Task, update: StatusUpdate)

}

trait TaskHandlerFactory extends TaskStatus {

  private def lruCache(maxSize: Int) = {
    new LinkedHashMap[String, Lock](maxSize * 4 / 3, 0.75f, true) {
      override def removeEldestEntry(eldest: java.util.Map.Entry[String, Lock]): Boolean = {
        size() > maxSize
      }
    }
  }

  private val taskLockCache = lruCache(12)

  def getTaskType: String
  def getSupportedStatuses: Set[Int]
  def getHandler[T >: TaskHandler](status: Int): Option[T]

  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: String) {
    val l: Lock = taskLockCache.synchronized {
      if (!taskLockCache.containsKey(task.id)) {
        taskLockCache.put(task.id, new ReentrantLock())
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

trait TaskHandlerFactoryFactory {
  def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T]
  def getSupportedFactories: List[TaskHandlerFactory]
  def getHandler(taskType: String, status: Int): Option[TaskHandler]
}

object TaskHandlerFactoryFactory {
  def apply(factories: TaskHandlerFactory*): TaskHandlerFactoryFactory = new TaskHandlerFactoryFactory {

    def getSupportedFactories: List[TaskHandlerFactory] = factories.toList

    def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T] = {
      factories.find(tf => tf.getTaskType == taskType).map(_.asInstanceOf[T])
    }

    def getHandler(taskType: String, status: Int): Option[TaskHandler] = {
      val f = getSupportedFactories.find(tf => tf.getTaskType == taskType)
      f.flatMap(_.getHandler(status))
    }
  }

}

object RelativeScheduledStatusUpdate {

  def apply(nextStatus: Int, minutesInFuture: Int, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None): ScheduledStatusUpdate = {
    ScheduledStatusUpdate(nextStatus, createFutureDate(minutesInFuture), newStringPayload, newIntValue)
  }

  private def createFutureDate(minutesInFuture: Int): Date = {
    val now = new Date()
    new Date(now.getTime + (minutesInFuture * 1000 * 60))
  }
}

case class StatusUpdate(nextStatus: Int, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None, attemptCount: Int = 0) extends HandlerUpdate
case class ScheduledStatusUpdate(nextStatus: Int, scheduleAfter: Date, newStringPayload: Option[String] = None, newIntValue: Option[Int] = None) extends HandlerUpdate

