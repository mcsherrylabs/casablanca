package sss.casablanca.webservice.remotetasks

import spray.json._
import sss.casablanca.util.JsonMapper

import scala.language.implicitConversions

case class SimpleChildTaskTrackingPayload(payload: String, val remoteTasks: Map[String, String]) extends ChildTaskTrackingPayload

object SimpleChildTaskTrackingPayloadJsonMapper extends DefaultJsonProtocol with JsonMapper[SimpleChildTaskTrackingPayload, String] {

  implicit val SimpleChildTaskTrackingPayloadFormat = jsonFormat2(SimpleChildTaskTrackingPayload)

  implicit override def to(jsonStr: String): SimpleChildTaskTrackingPayload = {
    val js = jsonStr.parseJson
    js.convertTo[SimpleChildTaskTrackingPayload]
  }

  implicit override def from(simpleChildTaskTrackingPayload: SimpleChildTaskTrackingPayload): String = {
    val js = simpleChildTaskTrackingPayload.toJson
    js.compactPrint
  }
}