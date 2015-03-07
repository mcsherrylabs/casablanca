package casablanca.task

import org.scalatest._
import java.util.Date
import spray.json._
import DefaultJsonProtocol._
import casablanca.webservice.remotetasks._

class TaskJsonSpec extends FlatSpec with Matchers {

  "Spray " should " be able to case class a json str " in {
    
    val createDate = new Date()
    val scheduleDate = new Date()
    
	  val jTask = new JsonTask(Some("parentNode"),
			  Some("parentTaskId"),
			  "id",
			  createDate,
			  Some(scheduleDate),
			  "taskType",
			  45,
			  4,
			  "strPayload: String")
    
    val str = TaskJsonMapper.fromTask(jTask)
    println(str)
    val t = TaskJsonMapper.toTask(str)
    assert(t == jTask)
  }
  
  
  it should " be able to deal with missing optional values" in {
    
    val createDate = new Date()
    
    
	  val jTask = new JsonTask(None,
			  None,
			  "id",
			  createDate,
			  None,
			  "taskType",
			  45,
			  4,
			  "strPayload: String")
    
    val str = TaskJsonMapper.fromTask(jTask)
    println(str)
    val t = TaskJsonMapper.toTask(str)
    assert(t == jTask)
  }

}