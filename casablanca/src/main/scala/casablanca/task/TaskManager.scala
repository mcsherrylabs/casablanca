package casablanca.task

import casablanca.util.Configure
import casablanca.db._
import java.util.UUID
import java.util.Date

class TaskManager(configName: String) extends Configure {
	
  private val myConfig = config(configName)

  private val db = new Db(myConfig.getString("db"))
  private val taskTable = db.table(myConfig.getString("taskTableName"))
  
  def create(taskType: String, status: Int, strPayload: String = "", intPayload: Int = 0, scheduleTime: Option[Date] = None): Task = {
    val uuid  = UUID.randomUUID.toString
    taskTable.insert(uuid, new Date(), taskType, status, 0, scheduleTime)
    getTask(uuid)
  }
  
  def getTask(taskId: String): Task = {
    val results = taskTable.filter(s" taskId = '${taskId}'")
    if(results.size != 1) throw new Error("Too many taskIds ")
    else new TaskImpl(results.rows(0))
  }
  
  def updateTaskStatus(taskId: String, statusUpdate: StatusUpdate): Task = {    
    val scheduleTimeUpdate = statusUpdate.scheduleAfter match {
      case None => ""
      case Some(schedule) => s", scheduleTime = ${schedule.getTime}"
    }
    taskTable.update(s"status = ${statusUpdate.nextStatus}, attemptCount = 0, createTime = ${(new Date().getTime)} ${scheduleTimeUpdate}", s"taskId = '${taskId}' ")
    getTask(taskId)
  }
  
  def incAttempts(task: Task): Task = {
    taskTable.update(s"attemptCount = ${task.attemptCount + 1}", s"taskId = '${task.id}'")
    val t = getTask(task.id)
    println(s"Count ${t.attemptCount}")
    t
  }

  def findScheduledTasks(beforeWhen: Date): List[Task] = {
    val res = taskTable.filter(s" scheduleTime <= ${beforeWhen.getTime} ORDER BY createTime ASC")
    println(s"Finding tasks before ${beforeWhen}")    
    res.map( new TaskImpl( _))
  }
  
  def findTasks(taskType: String, status: Int, latestTaskTimestamp: Option[Date]): List[Task] = {  

    // use the last time received. 
    val createTimeClause = latestTaskTimestamp match {
      case Some(dt) => s" AND createTime > ${dt.getTime} "
      case None => ""
    }
        
    val res = taskTable.filter(s" taskType = '${taskType}' AND status = ${status} ${createTimeClause} AND scheduleTime IS NULL ORDER BY createTime ASC")
    println(s"Finding tasks after ${latestTaskTimestamp map { _.getTime}}")    
    res.map( new TaskImpl( _))
    
  }  
  
  def close = db.shutdown
  
}