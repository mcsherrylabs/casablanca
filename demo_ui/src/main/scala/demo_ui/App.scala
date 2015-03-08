package demo_ui

import com.twitter.finatra._
import java.nio.file.{Files, Paths}
import java.io.InputStream
import com.stackmob.newman.ApacheHttpClient
import com.stackmob.newman.dsl._
import com.stackmob.newman._

import com.stackmob.newman.dsl._
import com.stackmob.newman._
import java.net.URL
import scala.concurrent._
import scala.concurrent.duration._



class UpdateWorld extends Controller  {

  protected implicit val httpClient = new ApacheHttpClient
  
  private def toByteArray(name: String) = {
    import scala.language.postfixOps
    val in: InputStream = this.getClass().getResourceAsStream(name);
    Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray    
  }
  
  private val redByteArray = toByteArray("/red-square.jpg")    
  private val greenByteArray = toByteArray("/green-square.jpg")
  
  private def genRow:Map[Int, Boolean] = {
    ((1 to 4) map { i: Int =>
      (i -> false)
    }).toMap  
  }
  
  private var world : Map[Int, Map[Int, Boolean]] = {
    ((1 to 4) map { i: Int =>
      (i -> genRow)
    }).toMap
  }
  
  get("/update/:row/:col/:onoff") { request =>
    log.info("update called ...")
    val row = request.routeParams("row").toInt
    val col = request.routeParams("col").toInt
    val onOff = request.routeParams("onoff").toBoolean
    val cols = world(row)
    val newCols = cols + (col -> onOff)
    world = world + (row -> newCols)
    render.status(200).toFuture
  }
  
  get("/start/:row/:col") { request =>
    val row = request.routeParams("row").toInt
    val col = request.routeParams("col").toInt
    log.info(s"Starting panel ${row} ${col}")
    val url = new URL(s"http://localhost:7${row}7${col}/task/demoTask")
    val p = POST(url)
    p.apply
    //val response = Await.result(p.apply, 10.second) //this will throw if the response doesn't return within  seconds
  	redirect("/").toFuture
  }
  
  get("/square/red.jpg") { request =>
    
    render.status(200)
        .contentType("application/octet-stream")
        .body(redByteArray)
        .toFuture
  }
  
  get("/square/green.jpg") { request =>
    
    render.status(200)
        .contentType("application/octet-stream")
        .body(greenByteArray)
        .toFuture
  }
  
  get("/") { request =>
    
    render.html(paintWorld).toFuture
  }
  

  private val htmlHead = "<html><head><meta http-equiv='refresh' content='1'></head><body><center><table>"
  private val htmlTail = "</table></center></body></html>"
    
  private def paintRow(rowIndex: Int, row: Map[Int, Boolean]) = {
    row.foldLeft("")((a,rowElem) => {
      val push = s"/start/${rowIndex}/${rowElem._1}"
      val greenOrRed = if(rowElem._2) "/square/red.jpg" else "/square/green.jpg"
      a + s"<td><a  href='${push}'><img src='${greenOrRed}'/></a></td>" 
    })       
  }
  
  def paintWorld: String = {
    var rowIndex = 0
    val middle = world.foldLeft("")((a,row) => { 
      rowIndex += 1
      a + s"<tr>${paintRow(rowIndex, row._2)}</tr>"      
    })
    htmlHead + middle + htmlTail  
  }
}

object App extends FinatraServer {
  register(new UpdateWorld ())
}