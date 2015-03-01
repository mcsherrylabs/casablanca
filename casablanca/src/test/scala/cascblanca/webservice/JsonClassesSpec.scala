package casablanca.task

import org.scalatest._
import java.util.Date
import spray.json._
import DefaultJsonProtocol._
import casablanca.webservice.remotetasks._

class JsonClassesSpec extends FlatSpec with Matchers {

  import RemoteTaskWithPayloadJsonProtocol._

  "Spray " should " be able to case class a json str " in {
    val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \" { \\\"some\\\": \\\"valid\\\" } \" }"
    val js = str.parseJson
    println("IS " + js)
    js.convertTo[RemoteTaskDecorator] match {
      case RemoteTaskDecorator(" { \"some\": \"valid\" } ", "localhost:7070", None, Some("mailerTask")) =>
      case x => fail(s"Didnt match ${x}")
    }

  }

  "Spray 2 " should " be able to case class a json str 2 " in {
    val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \" { \\\"some\\\": \\\"valid\\\" } \" }"

    val js2 = str.parseJson
    println("IS2 " + js2)
    js2.convertTo[RemoteTaskDecorator] match {
      case RemoteTaskDecorator(payload, "localhost:7070", None, Some("mailerTask")) => {
        println("Payload " + payload)
        val parsed = payload.parseJson
        println("Parsed " + parsed)

      }
      case x => fail(s"Didnt match ${x}")
    }

  }
  it should " be able to json case class " in {
    //val str = "{ \"node\" : \"localhost:7070\", \"taskType\" : \"mailerTask\", \"strPayload\" : \"\" }"
    val cc = RemoteTaskDecorator("{ \"some\": \"valid\" }", "localhost:7070", None, Some("mailerTask"))
    val asStr = cc.toJson

    asStr.convertTo[RemoteTaskDecorator] match {
      case RemoteTaskDecorator("{ \"some\": \"valid\" }", "localhost:7070", None, Some("mailerTask")) =>
      case x => fail(s"Didnt match ${x}")
    }
  }

}