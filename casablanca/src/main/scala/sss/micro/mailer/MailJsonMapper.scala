package sss.micro.mailer

import spray.json._
import DefaultJsonProtocol._
import java.util.Date
import casablanca.util.JsonMapper
import scala.language.implicitConversions

object MailJsonMapper extends DefaultJsonProtocol with JsonMapper[Mail, String] {

  implicit object MailJsonFormat extends RootJsonFormat[Mail] {

    val name = "name"
    val domain = "domain"
    val mailerConfigName = "mailerConfigName"
    val from = "from"
    val to = "to"
    val subject = "subject"
    val body = "body"

    def write(m: Mail) = {

      JsObject(Map(
        mailerConfigName -> JsString(m.mailerConfigName),
        from -> JsObject(Map(
          name -> JsString(m.from.name),
          domain -> JsString(m.from.domain))),
        to -> JsObject(Map(
          name -> JsString(m.to.name),
          domain -> JsString(m.to.domain))),
        subject -> JsString(m.subject),
        body -> JsString(m.body)))

    }

    def read(value: JsValue) = {

      println("WHA " + value.compactPrint)

      val jsObj = value.asJsObject
      jsObj.getFields(mailerConfigName, from, to, subject, body) match {
        case Seq(JsString(mailerConfigName), fromObj, toObj, JsString(subject), JsString(body)) => {
          val fromEmail = fromObj.asJsObject.getFields(from, to) match {
            case Seq(Vector(JsString(fromName), JsString(fromDomain))) => Email(fromName, fromDomain)
            case x => throw new DeserializationException(s"Not expecting ${x}")
          }
          val toEmail = toObj.asJsObject.getFields(from, to) match {
            case Seq(JsString(toName), JsString(toDomain)) => Email(toName, toDomain)
            case x => throw new DeserializationException(s"Not expecting ${x}")
          }
          Mail(fromEmail, toEmail, subject, body, mailerConfigName)
        }
        case x => throw new DeserializationException(s"Not expecting ${x}")
      }

    }
  }

  implicit override def to(jsonStr: String): Mail = {
    val js = jsonStr.parseJson
    js.convertTo[Mail]
  }

  implicit override def from(t: Mail): String = {
    val js = t.toJson
    js.compactPrint
  }
}
