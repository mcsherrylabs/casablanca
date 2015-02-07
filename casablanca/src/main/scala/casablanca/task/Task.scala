package casablanca.task

import java.util.Date
import casablanca.db.Row


trait Task {
  val id : String
  val createTime : Date
  val taskType : String
  val status : Int
  val attemptCount : Int
  
}

class TaskImpl(row : Row) extends Task {
   val id: String = row("taskId") 
   val status: Int = row("status")
   val taskType: String = row("taskType")
   val createTime : Date = {
     val asLong : Long = row("createTime")
     new Date(asLong)
   }
   val attemptCount : Int  = row("attemptCount")
} 
