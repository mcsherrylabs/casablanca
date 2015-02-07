package casablanca.util

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

trait Configure {

  implicit val config = ConfigFactory.load()
  def config(name: String): Config = config.getConfig(name)
}

object ConfigureFactory extends Configure 