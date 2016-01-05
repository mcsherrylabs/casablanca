package sss.casablanca.task

import java.util.Date

import org.scalatest._

class TaskJsonSpec extends FlatSpec with Matchers {

  "Spray " should " be able to case class a json str " in {

    val createDate = new Date()
    val scheduleDate = new Date()

    val jTask = new JsonTask(Some("parentNode"),
      Some("parentTaskId"),
      "id",
      createDate,
      Some(scheduleDate),
      "taskType",
      45,
      4,
      "strPayload: String")

    val str = TaskJsonMapper.from(jTask)
    println(str)
    val t = TaskJsonMapper.to(str)
    assert(t == jTask)
  }

  it should " be able to deal with missing optional values" in {

    val createDate = new Date()

    val jTask = new JsonTask(None,
      None,
      "id",
      createDate,
      None,
      "taskType",
      45,
      4,
      "strPayload: String")

    val str = TaskJsonMapper.from(jTask)
    println(str)
    val t = TaskJsonMapper.to(str)
    assert(t == jTask)
  }

}