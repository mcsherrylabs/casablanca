package sss.micro.mailer

import spray.json._
import DefaultJsonProtocol._
import java.util.Date
import casablanca.util.JsonMapper
import scala.language.implicitConversions

object MailJsonMapper extends DefaultJsonProtocol with JsonMapper[Mail, String] {

  val name = "name"
  val domain = "domain"
  val mailerConfigName = "mailerConfigName"
  val from = "from"
  val to = "to"
  val subject = "subject"
  val body = "body"

  implicit val emailFormat = jsonFormat2(Email)
  implicit val mailFormat = jsonFormat(Mail, from, to, subject, body, mailerConfigName)

  implicit override def to(jsonStr: String): Mail = {
    val js = jsonStr.parseJson
    js.convertTo[Mail]
  }

  implicit override def from(m: Mail): String = {
    val js = m.toJson
    js.compactPrint
  }
}
