package sss.casablanca.demo

import com.twitter.finatra._
import java.nio.file.{ Files, Paths }
import java.io.InputStream
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import com.stackmob.newman._

import com.stackmob.newman.dsl._
import com.stackmob.newman._
import java.net.URL
import scala.concurrent._
import scala.concurrent.duration._

class UpdateWorld extends Controller {

  protected implicit val httpClient = new ApacheHttpClient

  private def genRow: Map[Int, Int] = {
    ((1 to 4) map { i: Int =>
      (i -> 0)
    }).toMap
  }

  private var world: Map[Int, Map[Int, Int]] = {
    ((1 to 4) map { i: Int =>
      (i -> genRow)
    }).toMap
  }

  get("/update/:row/:col/:onoff") { request =>
    log.info("update called ...")
    val row = request.routeParams("row").toInt
    val col = request.routeParams("col").toInt
    val onOff = request.routeParams("onoff").toBoolean
    synchronized {
      val cols = world(row)
      val newVal = if (onOff) {
        cols(col) + 1
      } else {
        if (cols(col) - 1 < 0) 0 else cols(col) - 1
      }

      val newCols = cols + (col -> newVal)
      world = world + (row -> newCols)
    }
    render.status(200).toFuture
  }

  get("/start/:row/:col") { request =>
    val row = request.routeParams("row").toInt
    val col = request.routeParams("col").toInt
    log.info(s"Starting panel ${row} ${col}")
    val url = new URL(s"http://localhost:7${row}7${col}/task/demoTask")
    val p = POST(url).addBody("1")
    p.apply

    redirect("/index.html").toFuture
  }

  get("/example") { request =>
    render.static("index.html").toFuture
  }

  get("/world") { request =>
    render.json(world).toFuture
  }

}

object App extends FinatraServer {
  register(new UpdateWorld())
}