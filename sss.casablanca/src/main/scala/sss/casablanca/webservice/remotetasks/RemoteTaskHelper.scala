package sss.casablanca.webservice.remotetasks

import spray.json.DefaultJsonProtocol._
import spray.json._
import sss.casablanca.util.JsonMapper

import scala.language.implicitConversions

case class RemoteTaskDecorator(
  strPayload: String,
  node: String,
  parentTaskId: Option[String],
  taskId: String,
  taskType: String)

object RemoteTaskHelper extends JsonMapper[String, RemoteTaskDecorator] {

  implicit val remoteTaskWithPayloadFormat = jsonFormat5(RemoteTaskDecorator)

  implicit def from(msg: String): RemoteTaskDecorator = {
    val js = msg.parseJson
    js.convertTo[RemoteTaskDecorator]
  }

  implicit def to(remoteTaskDecorator: RemoteTaskDecorator): String = {
    val js = remoteTaskDecorator.toJson
    js.compactPrint
  }

}