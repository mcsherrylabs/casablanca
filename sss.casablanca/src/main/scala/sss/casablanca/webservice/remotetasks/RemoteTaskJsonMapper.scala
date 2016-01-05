package sss.casablanca.webservice.remotetasks

import spray.json._
import sss.casablanca.util.JsonMapper
import sss.casablanca.webservice.remotetasks.RemoteTaskHandlerFactory.RemoteTask

import scala.language.implicitConversions

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
