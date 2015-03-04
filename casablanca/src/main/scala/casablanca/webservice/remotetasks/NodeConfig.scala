package casablanca.webservice.remotetasks

import java.net.URL
import casablanca.util.Configure
import scala.collection.JavaConversions._
import com.typesafe.config.Config
import casablanca.util.Logging

object NodeConfig extends Configure with Logging {
  private val local = "local"

  lazy private val nodes = config.getConfig("nodes")

  lazy val localNode = nodes.getString(local)

  def map(node: String): String = nodes.getString(node)

}