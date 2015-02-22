package casablanca.webservice

import scala.util.Try

import com.twitter.finatra._
import com.twitter.finatra.ContentType._

import spray.json._
import DefaultJsonProtocol._ 

class Endpoint extends Controller {

    /**
     * Uploading files
     *
     * curl -F avatar=@/path/to/img http://localhost:7070/profile
     */
    post("/task") { request =>      
    val json = request.contentString.parseJson
    render.status(200).json(json.compactPrint).toFuture
   
    }

    error { request =>
      request.error match {
        case Some(e:ArithmeticException) =>
          render.status(500).plain("whoops, divide by zero!").toFuture
        case Some(e:UnsupportedMediaType) =>
          render.status(415).plain("Unsupported Media Type!").toFuture
        case _ =>
          render.status(500).plain("Something went wrong!").toFuture
      }
    }


    /**
     * Custom 404s
     *
     * curl http://localhost:7070/notfound
     */
    notFound { request =>
      render.status(404).plain("not found yo").toFuture
    }

  }