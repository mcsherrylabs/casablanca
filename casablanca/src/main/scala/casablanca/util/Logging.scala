package casablanca.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait Logging {
  val log: Logger = LoggerFactory.getLogger(this.getClass());

}

object LogFactory extends Logging {

  def getLogger(category: String): Logger = {
    LoggerFactory.getLogger(category)
  }


}