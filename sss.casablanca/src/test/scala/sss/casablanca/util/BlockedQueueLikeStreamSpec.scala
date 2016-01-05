package sss.casablanca.util

import org.scalatest._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

class BlockedQueueLikeStreamSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  class MyQueue extends BlockingQueueLikeStream[Int] {
    override def initialiseStream: Stream[Option[Int]] = {
      List(None, Some(1), Some(2), Some(3), None).toStream
    }
  }

  "QueueLike Stream " should " be able get all elements" in {

    val myTest = new MyQueue
    assert(myTest.poll() == None)
    assert(myTest.poll() == Some(1))
    assert(myTest.poll() == Some(2))
    assert(myTest.poll() == Some(3))
    assert(myTest.poll() == None)
  }

  "QueueLike Stream " should " be able get all elements in parallel " in {

    val myTest = new MyQueue

    val synchroSet =
      new mutable.HashSet[Option[Int]] with mutable.SynchronizedSet[Option[Int]]

    val all = for (i <- 0 to 10) yield {
      Future {
        synchroSet.add(myTest.poll())
      }
    }
    Await.result(Future.sequence(all), Duration.Inf)
    assert(synchroSet.size == 4)
    assert(synchroSet.contains(Some(3)))
    assert(synchroSet.contains(Some(2)))
    assert(synchroSet.contains(Some(1)))
    assert(synchroSet.contains(None))
  }

}