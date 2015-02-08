package casablanca.task

import java.util.Date
import casablanca.db.Row

case class TaskUpdate(val nextStatus: Int,  val scheduleAfter: Option[Date] = None, 
  val numAttempts: Int = 0)

trait Task {
  val id : String
  val createTime : Date
  val schedule : Option[Date]
  val taskType : String
  val status : Int
  val attemptCount : Int
    
  override def equals(other: Any): Boolean = {
    other match {
      case o: Task => o.id == id
      case _ => false
    }
  }  
  
  override def hashCode: Int = {
    (17 + id.hashCode) * 32 
  }
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
   
   override def toString: String = {
     s" ${id} ${status} ${taskType} ${attemptCount} ${createTime} ${schedule}"
   }
} 
