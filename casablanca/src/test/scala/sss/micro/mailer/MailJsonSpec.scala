package sss.micro.mailer

import org.scalatest._
import java.util.Date
import spray.json._

import casablanca.webservice.remotetasks._

class MailJsonSpec extends FlatSpec with Matchers {

  "Spray " should " be able to case class a json str " in {

    val m = Mail(Email("me", "here.com"), Email("you", "here.com"), "subject", "body")

    val str: String = MailJsonMapper.from(m)
    println(str)
    val mail: Mail = MailJsonMapper.to(str)
    assert(mail == m)
  }

}