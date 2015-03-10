package casablanca.util

import org.scalatest._
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskHandlerFactory

object myTest extends BaseTaskHandlerFactory {
  def getTaskType = "it's me!"
}

class ReflectionUtilsSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  "Util " should " be able to  load an object by name " in {

    val taskHandlerFactory = ReflectionUtils.getInstance[TaskHandlerFactory]("casablanca.util.myTest")

    assert(taskHandlerFactory.getTaskType == "it's me!")
  }

}