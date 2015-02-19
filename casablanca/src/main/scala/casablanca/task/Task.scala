package casablanca.task

import java.util.Date
import casablanca.db.Row

case class TaskUpdate(val nextStatus: Int,  strPayload : Option[String] = None, intValue: Option[Int] = None, scheduleAfter: Option[Date] = None, 
  numAttempts: Int = 0)

trait Task {
  val id : String
  val createTime : Date
  val schedule : Option[Date]
  val taskType : String
  val status : Int
  val attemptCount : Int
  val strPayload : String
  val intValue: Int
    
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
     s" ${id} ${status} ${taskType} ${attemptCount} ${createTime} ${schedule} ${intValue} ${strPayload}"
  }
 
}

abstract class BaseTask(t: Task) extends Task {
  val id : String = t.id
  val createTime : Date = t.createTime
  val schedule : Option[Date] = t.schedule
  val taskType : String = t.taskType
  val status : Int = t.status
  val attemptCount : Int = t.attemptCount 
  val strPayload : String = t.strPayload 
  val intValue: Int = t.intValue
}

class TaskImpl(row : Row) extends Task {
   val id: String = row("taskId") 
   val status: Int = row("status")
   val taskType: String = row("taskType")
   val createTime : Date = {
     val asLong : Long = row("createTime")
     new Date(asLong)
   }
   val schedule : Option[Date] = {
     
    val asLong : Long = row("scheduleTime")
    if(asLong == 0) None
    else Some(new Date(asLong))
     
   }
   
   val attemptCount : Int  = row("attemptCount")
   
   val intValue : Int = row("intValue")
   val strPayload : String = row("strPayload")
 
} 
