package casablanca.task

import casablanca.util.Configure
import casablanca.db._
import java.util.UUID
import java.util.Date
import com.typesafe.config.Config

trait CreateTask {
  def create(descriptor: TaskDescriptor,
    scheduleTime: Option[TaskSchedule] = None,
    parent: Option[TaskParent] = None): Task
}

class TaskManager(configIt: Config) extends CreateTask {

  private val myConfig = configIt.getConfig("TaskManager")

  private val db = new Db(myConfig.getString("db"))
  private val taskTable = db.table(myConfig.getString("taskTableName"))

  override def create(descriptor: TaskDescriptor,
    schedule: Option[TaskSchedule] = None,
    parent: Option[TaskParent]): Task = {

    val uuid = UUID.randomUUID.toString
    taskTable.insert(parent.map(_.node), parent.map(_.taskId), uuid, new Date(), descriptor.taskType, descriptor.status.value, 0, schedule.map(_.when), descriptor.strPayload)
    getTask(uuid)
  }

  def getTask(taskId: String): Task = {
    val results = taskTable.filter(s" taskId = '${taskId}'")
    if (results.size != 1) throw new Error(s"Too many taskIds for ${taskId}")
    else fromRow(results.rows(0))
  }

  def updateTaskStatus(taskId: String, taskUpdate: TaskUpdate): Task = {

    val scheduleTimeUpdate = taskUpdate.scheduleAfter match {
      case None => s", scheduleTime = NULL"
      case Some(schedule) => s", scheduleTime = ${schedule.getTime}"
    }

    val strPayloadUpdate = s", strPayload = '${taskUpdate.strPayload}'"

    val sql = s"status = ${taskUpdate.nextStatus}, attemptCount = ${taskUpdate.numAttempts}, createTime = ${(new Date().getTime)} ${scheduleTimeUpdate} ${strPayloadUpdate} "
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
    res.map(t => fromRow(t))
  }

  def close = db.shutdown

  def fromRow(r: Row): Task = new TaskImpl(r)

}