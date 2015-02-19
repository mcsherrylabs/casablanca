package mindsandmachines

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Props
 
object Boot extends App {
 
  // create our actor system with the name smartjava
  implicit val system = ActorSystem("smartjava")
  val service = system.actorOf(Props[SJServiceActor], "sj-rest-service")
 
  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}
