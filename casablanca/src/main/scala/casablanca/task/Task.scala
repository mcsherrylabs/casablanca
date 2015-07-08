package casablanca.task

import java.util.Date
import _root_.sss.db.Row

case class TaskUpdate(nextStatus: Int, strPayload: String, scheduleAfter: Option[Date] = None,
  numAttempts: Int)

case class EventOrigin(taskId: String, taskType: String)
case class TaskEvent(eventPayload: String, origin: Option[EventOrigin] = None)

trait Task {
  val parentNode: Option[String]
  val parentTaskId: Option[String]
  val id: String
  val createTime: Date
  val schedule: Option[Date]
  val taskType: String
  val status: Int
  val attemptCount: Int
  val strPayload: String

  override def equals(other: Any): Boolean = {
    other match {
      case o: Task => o.id == id
      case _ => false
    }
  }

  override def hashCode: Int = {
    (17 + id.hashCode) * 32
  }

  override def toString: String = {
    s" ${parentNode} ${parentTaskId} ${id} ${status} ${taskType} ${attemptCount} ${createTime} ${schedule} ${strPayload}"
  }

}

abstract class BaseTask(t: Task) extends Task {
  val id: String = t.id
  val createTime: Date = t.createTime
  val schedule: Option[Date] = t.schedule
  val taskType: String = t.taskType
  val status: Int = t.status
  val attemptCount: Int = t.attemptCount
  val strPayload: String = t.strPayload

  val parentNode: Option[String] = t.parentNode
  val parentTaskId: Option[String] = t.parentTaskId
}

class TaskImpl(row: Row) extends Task {
  val id: String = row("taskId")
  val status: Int = row("status")
  val taskType: String = row("taskType")
  val createTime: Date = {
    val asLong: Long = row("createTime")
    new Date(asLong)
  }
  val schedule: Option[Date] = {

    val asLong: Long = row("scheduleTime")
    if (asLong == 0) None
    else Some(new Date(asLong))

  }

  val attemptCount: Int = row("attemptCount")

  val strPayload: String = row("strPayload")

  val parentNode: Option[String] = row[String]("parentNode") match {
    case null => None
    case "" => None
    case x => Some(x)
  }
  val parentTaskId: Option[String] = row[String]("parentTaskId") match {
    case null => None
    case "" => None
    case x => Some(x)
  }

}
