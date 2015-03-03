package casablanca.webservice.remotetasks

import spray.json._
import DefaultJsonProtocol._

case class RemoteTaskDecorator(strPayload: String, node: String,
  taskId: Option[String] = None,
  taskType: Option[String] = None)

object RemoteTaskHelper {

  implicit val remoteTaskWithPayloadFormat = jsonFormat4(RemoteTaskDecorator)

  def toRemoteTaskDecorator(msg: String): RemoteTaskDecorator = {
    val js = msg.parseJson
    js.convertTo[RemoteTaskDecorator]
  }

  def fromRemoteTaskDecorator(decorator: RemoteTaskDecorator): String = {
    val js = decorator.toJson
    js.compactPrint
  }

}