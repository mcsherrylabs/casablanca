package casablanca.task

import casablanca.util.Configure
import casablanca.db._
import java.util.UUID
import java.util.Date
import com.typesafe.config.Config
import casablanca.util.ProgrammingError
import java.sql.Connection

trait CreateTask {
  def create(descriptor: TaskDescriptor,
    scheduleTime: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task
}

class TaskManager(configIt: Config) extends CreateTask {

  private val myConfig = configIt.getConfig("taskManager")

  private val db = new Db(myConfig.getString("db"))
  private val taskTable = db.simpleTable(myConfig.getString("taskTableName"))

  def create(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent]): Task = {
    tx(createTx(descriptor, schedule, parent))
  }

  def createTx(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent]): Task = {

    val uuid = UUID.randomUUID.toString
    taskTable.insertTx(parent.map(_.node), parent.map(_.taskId), uuid, new Date(), descriptor.taskType, descriptor.status.value, 0, schedule.map(_.when), descriptor.strPayload)
    getTask(uuid)
  }

  def getTask(taskId: String): Task = {
    val results = taskTable.filter(s" taskId = '${taskId}'")
    if (results.size != 1) throw new ProgrammingError(s"There are ${results.size} taskIds (${taskId}) in the database! ")
    else fromRow(results.rows(0))
  }

  def updateTaskStatus(taskId: String, taskUpdate: TaskUpdate): Task = {
    tx(updateTaskStatusTx(taskId, taskUpdate))
  }

  def updateTaskStatusTx(taskId: String, taskUpdate: TaskUpdate): Task = {

    val scheduleTimeUpdate = taskUpdate.scheduleAfter match {
      case None => s", scheduleTime = NULL"
      case Some(schedule) => s", scheduleTime = ${schedule.getTime}"
    }

    val strPayloadUpdate = s", strPayload = '${taskUpdate.strPayload}'"

    val sql = s"status = ${taskUpdate.nextStatus}, attemptCount = ${taskUpdate.numAttempts}, createTime = ${(new Date().getTime)} ${scheduleTimeUpdate} ${strPayloadUpdate} "
    taskTable.updateTx(sql, s"taskId = '${taskId}' ")
    getTask(taskId)
  }

  def deleteTasksTx(status: Int, beforeWhen: Date): Int = {
    val sql = s" createTime <= ${beforeWhen.getTime} AND status = ${status}"
    taskTable.deleteTx(sql)
  }

  def deleteTasks(status: Int, beforeWhen: Date): Int = tx[Int](deleteTasksTx(status, beforeWhen))

  def tx[T] = taskTable.inTransaction[T] _

  def deleteTask(taskId: String) = tx(deleteTaskTx(taskId))

  def deleteTaskTx(taskId: String): Boolean = {
    val sql = s" taskId = ${taskId} "
    //taskTable.inTransaction[Int] { taskTable.delete(sql) } == 1
    taskTable.deleteTx(sql) == 1
  }

  def deleteTask(t: Task): Boolean = deleteTask(t.id)

  def findScheduledTasks(beforeWhen: Date): List[Task] = {
    val sql = s" scheduleTime <= ${beforeWhen.getTime} ORDER BY createTime ASC"
    findTasksImpl(sql)
  }

  def findChildren(parentTaskId: String, taskType: Option[String] = None): List[Task] = {
    val taskTypeSql = taskType match {
      case None => ""
      case Some(tt) => s"AND taskType = '${tt}'"
    }

    findTasksImpl(s" parentTaskId = '${parentTaskId}' ${taskTypeSql} AND scheduleTime IS NULL ORDER BY createTime ASC")
  }

  def findTasks(taskType: String, status: Option[Int]): List[Task] = {

    val statusSql = status match {
      case None => ""
      case Some(st) => s"AND status = ${st}"
    }

    findTasksImpl(s" taskType = '${taskType}' ${statusSql} AND scheduleTime IS NULL ORDER BY createTime ASC")
  }

  private def findTasksImpl(sql: String): List[Task] = {
    val res = taskTable.filter(sql)
    res.map(t => fromRow(t))
  }

  def close = db.shutdown

  def fromRow(r: Row): Task = new TaskImpl(r)

}