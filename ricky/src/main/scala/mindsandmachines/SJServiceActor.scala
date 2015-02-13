package mindsandmachines

/**
 * @author alan
 */
 
import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol



object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val personFormat = jsonFormat3(Person)
}
import MyJsonProtocol._

 
case class Person(name: String, fistName: String, age: Long)
 
// simple actor that handles the routes.
class   SJServiceActor extends Actor with HttpService {
 
  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory = context
 
  // we don't create a receive function ourselve, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive = runRoute(aSimpleRoute ~ anotherRoute)
 
  
// handles the api path, we could also define these in separate files
  // this path respons to get queries, and make a selection on the
  // media-type.
  val aSimpleRoute = {
    path("path1") {
      get {
 
        // Get the value of the content-header. Spray
        // provides multiple ways to do this.
        headerValue({
          case x@HttpHeaders.`Content-Type`(value) => Some(value)
          case default => None
        }) {
          // the header is passed in containing the content type
          // we match the header using a case statement, and depending
          // on the content type we return a specific object
          header => header match {
 
            // if we have this contentype we create a custom response
            case ContentType(MediaType("application/vnd.type.a"), _) => {
              respondWithMediaType(`application/json`) {
                complete {
                  Person("Bob", "Type A", System.currentTimeMillis());
                }
              }
            }
 
            // if we habe another content-type we return a different type.
            case ContentType(MediaType("application/vnd.type.b"), _) => {
              respondWithMediaType(`application/json`) {
                complete {
                  Person("Bob", "Type B", System.currentTimeMillis());
                }
              }
            }
 
            // if content-types do not match, return an error code
            case default => {
              complete {
                HttpResponse(406);
              }
            }
          }
        }
      }
    }
  }
 
  // handles the other path, we could also define these in separate files
  // This is just a simple route to explain the concept
  val anotherRoute = {
    path("path2") {
      get {
        // respond with text/html.
        respondWithMediaType(`text/html`) {
          complete {
            // respond with a set of HTML elements
            <html>
              <body>
                <h1>Path 2</h1>
              </body>
            </html>
          }
        }
      }
    }
  }  
}  