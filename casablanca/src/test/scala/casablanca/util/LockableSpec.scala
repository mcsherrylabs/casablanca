package casablanca.util

import org.scalatest._
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await

import scala.concurrent.duration.Duration

class LockableSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  object myTest extends Lockable[Int]

  "Lockable " should " be able to prevent a parellel lock " in {

    var lockedcount = 0
    var endtime = 0l

    def stayLocked(timeout: Int) {
      assert(endtime <= new Date().getTime)
      lockedcount += 1
      println(s"stayLocked for ${timeout}")
      Thread.sleep(timeout)
      println(s"unblocked after ${timeout}")
      endtime = new Date().getTime
    }

    val lockedOneSec = () => stayLocked(1000)

    val start = new Date().getTime
    val all = for (i <- 0 to 3) yield {
      Future {
        myTest.doLocked[Unit](999, lockedOneSec, 4000)
      }
    }

    Await.result(Future.sequence(all), Duration.Inf)
    val dur = new Date().getTime - start
    assert(dur >= 4000)
    assert(dur < 4300)

  }

  it should " timeout when locked  " in {

    var lockedcount = 0

    def stayLocked(timeout: Int) {
      lockedcount += 1
      Thread.sleep(timeout)
    }

    val lockedOneSec = () => stayLocked(1000)

    val all = List(
      Future { myTest.doLocked[Unit](999, lockedOneSec, 4000) },
      Future {
        try {
          myTest.doLocked[Unit](999, lockedOneSec, 200)
          fail("Should have thrown ex")
        } catch {
          case e: LockTimeoutException => println("All good")
        }
      })

    Await.result(Future.sequence(all), Duration.Inf)
    assert(lockedcount == 1)

  }

  "Lockable " should " allow multiple parellel locks for different keys " in {

    var lockedcount = 0
    var endtime = 0l

    def stayLocked(timeout: Int) {
      assert(endtime <= new Date().getTime)
      lockedcount += 1
      println(s"stayLocked for ${timeout}")
      Thread.sleep(timeout)
      println(s"unblocked after ${timeout}")
      endtime = new Date().getTime
    }

    val lockedOneSec = () => stayLocked(1000)

    val start = new Date().getTime
    val all = for (i <- 0 to 3) yield {
      Future {
        myTest.doLocked[Unit](i, lockedOneSec, 10)
      }
    }

    Await.result(Future.sequence(all), Duration.Inf)
    val dur = new Date().getTime - start
    println(s"duration in parallel ${dur}")
    assert(dur >= 1000)
    assert(dur < 1010)

  }

}