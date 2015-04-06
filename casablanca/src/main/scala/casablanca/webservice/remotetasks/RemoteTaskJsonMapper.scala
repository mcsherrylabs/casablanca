package casablanca.webservice.remotetasks

import spray.json._
import DefaultJsonProtocol._
import java.util.Date
import casablanca.util.JsonMapper
import scala.language.implicitConversions
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory.RemoteTask

object RemoteTaskJsonMapper extends DefaultJsonProtocol with JsonMapper[RemoteTask, String] {

  private implicit val remoteTaskJsonMapperFormat = jsonFormat4(RemoteTask)

  implicit override def to(jsonStr: String): RemoteTask = {
    val js = jsonStr.parseJson
    js.convertTo[RemoteTask]
  }

  implicit override def from(remoteTask: RemoteTask): String = {
    val js = remoteTask.toJson
    js.compactPrint
  }
}
