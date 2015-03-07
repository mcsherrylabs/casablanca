package casablanca.task

import spray.json._
import DefaultJsonProtocol._
import java.util.Date

case class JsonTask( parentNode: Option[String],
   parentTaskId: Option[String],
   id: String,
   createTime: Date,
   schedule: Option[Date],
   taskType: String,
   status: Int,
   attemptCount: Int,
   strPayload: String) extends Task 
  

object TaskJsonMapper extends DefaultJsonProtocol {
  
  private val id = "id"
  private val taskType = "taskType"
  private val status = "status"  
  private val attemptCount = "attemptCount"
  private val createTime = "createTime"
  private val schedule = "schedule"
  private val strPayload = "strPayload"
  private val parentNode = "parentNode"
  private val parentTaskId = "parentTaskId"
  
  
  implicit object TaskJsonFormat extends RootJsonFormat[Task] {
     

    def write(t: Task) = {
      val all = List(
      t.parentNode.map(pn => parentNode -> JsString(pn)),
      t.parentTaskId.map(pt => parentTaskId -> JsString(pt)),      
      Some(id -> JsString(t.id)),
      Some(taskType -> JsString(t.taskType)),
      Some(status -> JsNumber(t.status)),
      Some(attemptCount  -> JsNumber(t.attemptCount)),
      Some(createTime  -> JsNumber(t.createTime.getTime)),
      t.schedule.map(s => schedule -> JsNumber(s.getTime)),
      Some(strPayload  -> JsString(t.strPayload)))
      
      JsObject(all.flatten.toMap)
      
    }
    
    def read(value: JsValue) = {
       
      val jsObj = value.asJsObject
      val mandatoryFields = jsObj.getFields(id, createTime, taskType, status, attemptCount,  strPayload)
      val parentNodeOpt = if(jsObj.fields.contains(parentNode )) {
        Some(jsObj.getFields(parentNode)(0).toString)
      } else None
      
      val parentIdOpt = if(jsObj.fields.contains(parentTaskId )) {
        Some(jsObj.getFields(parentTaskId)(0).toString)
      } else None
      
      val scheduleOpt = if(jsObj.fields.contains(schedule)) {
        jsObj.getFields(schedule)(0) match {
          case JsNumber(sch) => Some(new Date(sch.toLong))
          case x => throw new DeserializationException(s"Not expecting ${x} in schedule")
        }
      } else None
      
      mandatoryFields match {
        case Seq(JsString(tId), JsNumber(tCreate), JsString(tType), JsNumber(tStatus), JsNumber(tAttemptCount), JsString(tPayload)) =>
          new JsonTask(parentNodeOpt, parentIdOpt, tId, new Date(tCreate.toInt), scheduleOpt, tType, tStatus.toInt, tAttemptCount.toInt, tPayload)
        case x => throw new DeserializationException(s"Not expecting ${x}") 
      }
      
    }
  }
  
  def toTask(jsonStr:String): Task = {
    
    val js = jsonStr.parseJson
    js.convertTo[Task]
  }
  
  def fromTask(t: Task): String = {
    val js = t.toJson
    js.compactPrint
  }
}
