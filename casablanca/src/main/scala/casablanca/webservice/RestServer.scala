package casablanca.webservice
import com.twitter.finatra._
import casablanca.util.Configure
import com.typesafe.config.Config

class RestServer(config: Config, controllers: Controller*) extends FinatraServer {

  val finatraPackage: String = "com.twitter.finatra.config"
  val configPrefix: String = "restServer"

  System.setProperty(s"${finatraPackage}.adminPort", ":" + config.getString(s"${configPrefix}.adminPort"))
  //System.setProperty(s"${finatraPackage}.appName", config.getString(s"${configPrefix}.appName"))
  //  System.setProperty(s"${finatraPackage}.assetPath", config.getString(s"${configPrefix}.assetPath"))
  //  System.setProperty(s"${finatraPackage}.certificatePath", config.getString(s"${configPrefix}.certificatePath"))
  //  System.setProperty(s"${finatraPackage}.docRoot", config.getString(s"${configPrefix}.docRoot"))
  //  System.setProperty(s"${finatraPackage}.env", config.getString(s"${configPrefix}.env"))
  //  System.setProperty(s"${finatraPackage}.keyPath", config.getString(s"${configPrefix}.keyPath"))
  //  System.setProperty(s"${finatraPackage}.logLevel", config.getString(s"${configPrefix}.logLevel"))
  //  System.setProperty(s"${finatraPackage}.logNode", config.getString(s"${configPrefix}.logNode"))
  //  System.setProperty(s"${finatraPackage}.logPath", config.getString(s"${configPrefix}.logPath"))
  //  System.setProperty(s"${finatraPackage}.maxRequestSize", config.getString(s"${configPrefix}.maxRequestSize"))
  //  System.setProperty(s"${finatraPackage}.pidEnabled", config.getString(s"${configPrefix}.pidEnabled"))
  //  System.setProperty(s"${finatraPackage}.pidPath", config.getString(s"${configPrefix}.pidPath"))
  System.setProperty(s"${finatraPackage}.port", ":" + config.getString(s"${configPrefix}.port"))
  //  System.setProperty(s"${finatraPackage}.sslPort", config.getString(s"${configPrefix}.sslPort"))
  //  System.setProperty(s"${finatraPackage}.templatePath", config.getString(s"${configPrefix}.templatePath"))
  controllers.foreach(register(_))

}