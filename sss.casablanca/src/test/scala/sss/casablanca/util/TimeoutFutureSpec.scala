package sss.casablanca.util

import java.util.Date
import java.util.concurrent.Executors

import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TimeoutFutureSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  implicit val sched = Executors.newScheduledThreadPool(5) //> sched  : java.util.concurrent.ScheduledExecutorService = java.util.concurren

  "TimeoutFuture " should " timeout if not completed " in {
    val initial = "initial"
    val time = new Date()
    val f = new TimeoutFuture[String](initial, 100).get
    val r = Await.result(f, Duration.Inf)
    val dur = new Date().getTime() - time.getTime()
    println("Long " + dur)
    assert(r == initial)
    assert(dur >= 100)

  }

  it should " not wait if completed " in {
    val initial = "initial"
    val updated = "updated"
    val time = new Date()
    val tf = new TimeoutFuture[String](initial, 1000)
    tf.complete(updated)
    val f = tf.get

    val r = Await.result(f, Duration.Inf)
    val dur = new Date().getTime() - time.getTime()
    println("Short " + dur)
    assert(r == updated)

    assert(dur < 10)

  }

}