package casablanca.task

import casablanca.util.Configure
import casablanca.db._
import java.util.UUID
import java.util.Date

class TaskManager(configName: String) extends Configure {

  private val myConfig = config(configName)

  private val db = new Db(myConfig.getString("db"))
  private val taskTable = db.table(myConfig.getString("taskTableName"))

  //taskId , createTime taskType status attemptCount scheduleTime strPayload intValue 
  def create(taskType: String, status: Int, strPayload: String = "", intPayload: Int = 0,
    scheduleTime: Option[Date] = None,
    parentNode: Option[String] = None,
    parentTaskId: Option[String] = None): Task = {
    val uuid = UUID.randomUUID.toString
    taskTable.insert(parentNode, parentTaskId, uuid, new Date(), taskType, status, 0, scheduleTime, strPayload, intPayload)
    getTask(uuid)
  }

  def getTask(taskId: String): Task = {
    val results = taskTable.filter(s" taskId = '${taskId}'")
    if (results.size != 1) throw new Error("Too many taskIds ")
    else fromRow(results.rows(0))
  }

  def updateTaskStatus[T >: Task](taskId: String, statusUpdate: TaskUpdate): T = {
    val scheduleTimeUpdate = statusUpdate.scheduleAfter match {
      case None => s", scheduleTime = NULL"
      case Some(schedule) => s", scheduleTime = ${schedule.getTime}"
    }
    val intValueUpdate = statusUpdate.intValue match {
      case None => ""
      case Some(newIntValue) => s", intValue = ${newIntValue}"
    }

    val strPayloadUpdate = statusUpdate.strPayload match {
      case None => ""
      case Some(newPayload) => s", strPayload = '${newPayload}'"
    }

    val sql = s"status = ${statusUpdate.nextStatus}, attemptCount = ${statusUpdate.numAttempts}, createTime = ${(new Date().getTime)} ${scheduleTimeUpdate} ${strPayloadUpdate} ${intValueUpdate}"
    //println(s"Update task ${taskId} sql -> ${sql}")
    taskTable.update(sql, s"taskId = '${taskId}' ")
    getTask(taskId)
  }

  def findScheduledTasks(beforeWhen: Date): List[Task] = {
    val sql = s" scheduleTime <= ${beforeWhen.getTime} ORDER BY createTime ASC"
    findTasksImpl(sql)
  }

  def findTasks(taskType: String, status: Int): List[Task] = {
    findTasksImpl(s" taskType = '${taskType}' AND status = ${status} AND scheduleTime IS NULL ORDER BY createTime ASC")
  }

  private def findTasksImpl(sql: String): List[Task] = {
    val res = taskTable.filter(sql)
    // TODO, must change this to use task factory lookup to create 
    // the correct instance of a task for a task type.... 
    res.map(t => fromRow(t))
  }

  def close = db.shutdown

  def fromRow(r: Row): Task = new TaskImpl(r)

}