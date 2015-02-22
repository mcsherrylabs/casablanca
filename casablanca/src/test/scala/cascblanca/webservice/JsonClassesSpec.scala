package casablanca.task

import org.scalatest._
import java.util.Date
import spray.json._
import DefaultJsonProtocol._

case class RemoteTaskWithPayload(node: String, taskType: String, strPayload: String)
case class Payload(some: String)

object RemoteTaskWithPayloadJsonProtocol extends DefaultJsonProtocol {
  implicit val remoteTaskWithPayloadFormat = jsonFormat3(RemoteTaskWithPayload)
  implicit val payloadFormat = jsonFormat1(Payload)

}

class JsonClassesSpec extends FlatSpec with Matchers {

  import RemoteTaskWithPayloadJsonProtocol._

  "Spray " should " be able to case class a json str " in {
    val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \" { \\\"some\\\": \\\"valid\\\" } \" }"
    val js = str.parseJson
    println("IS " + js)
    js.convertTo[RemoteTaskWithPayload] match {
      case RemoteTaskWithPayload("localhost:7070", "mailerTask", " { \"some\": \"valid\" } ") =>
      case x => fail(s"Didnt match ${x}")
    }

  }

  "Spray 2 " should " be able to case class a json str 2 " in {
    val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \" { \\\"some\\\": \\\"valid\\\" } \" }"

    val js2 = str.parseJson
    println("IS2 " + js2)
    js2.convertTo[RemoteTaskWithPayload] match {
      case RemoteTaskWithPayload("localhost:7070", "mailerTask", payload) => {
        println("Payload " + payload)
        val parsed = payload.parseJson
        println("Parsed " + parsed)
        parsed.convertTo[Payload] match {
          case Payload("valid") =>
          case x => fail("")
        }
      }
      case x => fail(s"Didnt match ${x}")
    }

  }
  it should " be able to json case class " in {
    //val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \"\" }"
    val cc = RemoteTaskWithPayload("localhost:7070", "mailerTask", "{ \"some\": \"valid\" }")
    val asStr = cc.toJson

    asStr.convertTo[RemoteTaskWithPayload] match {
      case RemoteTaskWithPayload("localhost:7070", "mailerTask", "{ \"some\": \"valid\" }") =>
      case x => fail(s"Didnt match ${x}")
    }
  }

}