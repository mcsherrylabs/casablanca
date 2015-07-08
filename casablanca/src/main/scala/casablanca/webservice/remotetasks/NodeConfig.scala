package casablanca.webservice.remotetasks

import java.net.URL
import _root_.sss.ancillary.Configure
import scala.collection.JavaConversions._
import com.typesafe.config.Config
import _root_.sss.ancillary.Logging

class NodeConfig(config: Config)  extends Logging {
  private val local = "local"

  lazy private val nodes = config.getConfig("nodes")

  lazy val localNode = nodes.getString(local)

  def map(node: String): String = {
    // this could be jiggered to use a pool of nodes... 
    nodes.getString(node)  
  }

}