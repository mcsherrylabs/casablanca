package casablanca.task

import org.scalatest._
import java.util.Date



class TaskManagerSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  val tmUnderTest = new TaskManager("taskManager")
  
  val status = 4
  val intVal = 99
  val strPayload = "strPayload"
  val taskType = "type1"
      
  override def afterAll  {
    tmUnderTest.close
    println("TaskManager closed")
  }
  
  "TaskManager " should " be able to create a task " in {
    
    
    val aroundNow = new Date()
    
    val t = tmUnderTest.create(taskType, status, strPayload, intVal)
    assert(t.attemptCount == 0)
    assert(t.id != null)
    assert(t.status == status)
    assert(t.schedule == None)
    assert(t.createTime.getTime >=  aroundNow.getTime)
    assert(t.createTime.getTime <  aroundNow.getTime + 500)
    assert(t.intValue == intVal)
    assert(t.strPayload == strPayload)
    
  }
  
  it should " find the task (using get or find) " in {
    val foundTasks = tmUnderTest.findTasks(taskType, status)
    assert(foundTasks.size > 0)
    foundTasks map {
      t => {
        val retrievedById = tmUnderTest.getTask(t.id)
        
        assert(retrievedById == t)
        assert(retrievedById.attemptCount == t.attemptCount)
        assert(retrievedById.createTime == t.createTime)
        assert(retrievedById.schedule == None)
      }
    }
  }
  
  it should " update the task status " in {
    val t = tmUnderTest.create(taskType, status, strPayload, intVal)
    val taskUpdate = TaskUpdate(status + 1, Some("newStringPayload"), Some(7456))
    val updatedTask = tmUnderTest.updateTaskStatus(t.id, taskUpdate)
    assert(updatedTask.status == status + 1)
    val retrievedUpdatedTask = tmUnderTest.getTask(t.id)
    assert(retrievedUpdatedTask.status == status + 1)
    assert(retrievedUpdatedTask.intValue == 7456)
    assert(retrievedUpdatedTask.strPayload == "newStringPayload")
  }
 
   
  it should " increment the num task attempts " in {
    var task = tmUnderTest.create(taskType, status, strPayload, intVal)
    for(i <- 0 to 10) {
      assert(task.attemptCount == i)
      val taskUpdate = TaskUpdate(task.status, None, None, None, task.attemptCount + 1)
      task = tmUnderTest.updateTaskStatus(task.id, taskUpdate)    	
    }
    
  }
   
  it should " update the scheduleTime if specified " in {
    val t = tmUnderTest.create(taskType, status, strPayload, intVal)
    val scheduled = new Date(new Date().getTime + 100000)
    
    val taskUpdate = TaskUpdate(status + 1, None, None, Some(scheduled))
    val updatedTask = tmUnderTest.updateTaskStatus(t.id, taskUpdate)
    assert(updatedTask.status == status + 1)
    val retrievedUpdatedTask = tmUnderTest.getTask(t.id)
    assert(retrievedUpdatedTask.schedule == Some(scheduled))
  }
  
  
  it should " not find a task with a schedule " in {
    val t = tmUnderTest.create(taskType, status, strPayload, intVal, Some(new Date()))
    val foundTasks = tmUnderTest.findTasks(taskType, status)
    assert(foundTasks.size > 0)
    foundTasks map {
      foundTask => {        
        assert(foundTask != t)        
      }
    }
  }
  
  it should " find a task with a schedule via findScheduledTasks " in {
    val now = new Date()
    val afterNow = new Date(now.getTime + 1)
    val beforeNow = new Date(now.getTime - 1)
    val taskType = "scheduleTest"
      
    val t = tmUnderTest.create(taskType, status, strPayload, intVal, Some(now))
    val t2 = tmUnderTest.create(taskType, status, strPayload, intVal, Some(beforeNow))
    val t3 = tmUnderTest.create(taskType, status, strPayload, intVal, Some(afterNow))
    val t4 = tmUnderTest.create(taskType, status, strPayload, intVal, None )
    
    val foundTasks = tmUnderTest.findScheduledTasks(now)
    assert(foundTasks.size > 0)
    var foundIt = false
    var foundIt2 = false
    var notFoundIt = true
    var notFoundT4 = true
    
    foundTasks map {
      foundTask => {
        println(s"Found task ${foundTask}")
        if(foundTask == t) foundIt = true        
        if(foundTask == t2) foundIt2 = true
        if(foundTask == t3) notFoundIt = false
        if(foundTask == t4) notFoundT4 = false
        
      }
    }
    assert(foundIt)
    assert(foundIt2)
    assert(notFoundIt)
    assert(notFoundT4)
  }
  
  it should "  reset try count after status change " in {
    var task = tmUnderTest.create(taskType, status, strPayload, intVal)
    assert(task.attemptCount == 0)
    var taskUpdate = TaskUpdate(task.status, None, None, None, task.attemptCount + 1)
    task = tmUnderTest.updateTaskStatus(task.id, taskUpdate)
    assert(task.attemptCount == 1)    
    taskUpdate = TaskUpdate(status + 1)
    val updatedTask = tmUnderTest.updateTaskStatus(task.id, taskUpdate)
    assert(updatedTask.attemptCount == 0)
    
  }
}