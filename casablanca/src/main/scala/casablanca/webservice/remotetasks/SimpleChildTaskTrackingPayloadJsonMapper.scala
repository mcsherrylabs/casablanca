package casablanca.webservice.remotetasks

import spray.json._
import DefaultJsonProtocol._
import java.util.Date
import casablanca.util.JsonMapper
import scala.language.implicitConversions

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
