package sss.micro.mailer

import spray.json._
import DefaultJsonProtocol._
import java.util.Date

   
object MailJsonMapper extends DefaultJsonProtocol {
  

  
  def toMail(jsonStr:String): Mail  = {
    
    val js = jsonStr.parseJson
    //js.convertTo[MailTask]
    ???
  }
  
  def fromMail(t: Mail): String = {
    //val js = t.toJson
    ///js.compactPrint
    ???
  }
}
