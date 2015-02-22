package casablanca

import com.twitter.finatra._
import casablanca.webservice.Endpoint
import com.stackmob.newman._
import com.stackmob.newman.dsl._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL

object App extends FinatraServer {

  implicit val httpClient = new ApacheHttpClient
  //execute a GET request
  val myUrl = new URL("http://google.com")
  val response = Await.result(GET(myUrl).apply, 1.second) //this will throw if the response doesn't return within 1 second
  println(s"Response returned from ${myUrl.toString} with code ${response.code}, body ${response.bodyString}")
  register(new Endpoint())
}
