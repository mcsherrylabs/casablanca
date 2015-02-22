package casablanca.task

import org.scalatest._
import java.util.Date
import spray.json._
import DefaultJsonProtocol._

case class RemoteTaskWithPayload(node: String, taskType: String, strPayload: String)

object RemoteTaskWithPayloadJsonProtocol extends DefaultJsonProtocol {
  implicit val remoteTaskWithPayloadFormat = jsonFormat3(RemoteTaskWithPayload)
}

class JsonClassesSpec extends FlatSpec with Matchers {

  import RemoteTaskWithPayloadJsonProtocol._

  "Spray " should " be able to case class a json str " in {
    val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \"\" }"
    str.parseJson.convertTo[RemoteTaskWithPayload] match {
      case RemoteTaskWithPayload("localhost:7070", "mailerTask", "") =>
      case x => fail(s"Didnt match ${x}")
    }

  }

  it should " be able to json case class " in {
    //val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \"\" }"
    val cc = RemoteTaskWithPayload("localhost:7070", "mailerTask", "")
    val asStr = cc.toJson
    println(asStr)
    asStr.convertTo[RemoteTaskWithPayload] match {
      case RemoteTaskWithPayload("localhost:7070", "mailerTask", "") =>
      case x => fail(s"Didnt match ${x}")
    }
  }

}