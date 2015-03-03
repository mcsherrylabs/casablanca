package casablanca.webservice.remotetasks

import java.net.URL
import casablanca.util.Configure

object NodeConfig extends Configure {
  val thisNode = "http://localhost:7070"
  def toUrl(node: String): URL = new URL("http://localhost:8282")
}