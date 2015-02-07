package casablanca

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import casablanca.task.TaskManager
import casablanca.task.StatusQConsumer


object TestTaskManager extends App {

  val tm = new TaskManager("taskManager")
  
  for(i <- 0 to 10) tm.create("exampleType", 0)
  
  val q = tm.taskQueue("exampleType", 0)
  val q2 = tm.taskQueue("exampleType", 1)
  val q3 = tm.taskQueue("exampleType", 2)
//  
//  val sc = new StatusConsumer(q, "consumerOfType1", 10, 10)
//  val sc1 = new StatusConsumer(q2, "consumerOftype1", 10, 10)
//  val sc2 = new StatusConsumer(q3, "consumerOfType2", 10, 10)
//  
//  sc.start
//  sc1.start
//  sc2.start
//  
//  Thread.sleep(5000)
//  
//  println("Stopping")
//  sc.stop
//  sc1.stop
//  sc2.stop
//  
  
  /*val f3 = future {
  
    for(i <- 0 to 10) {
	  q3.poll(2000, "consumerIdentifier") map { t =>
	    println(s"Q3  Got one! ${t.id} ${t.createTime.getTime}")	    
	  }
    }
    
  }
  
  val f1 = future {
  
    for(i <- 0 to 100) {
	  q.poll(100, "consumerIdentifier") map { t =>
	    println(s"   Got one! ${t.id} ${t.createTime.getTime}")
	    q.push(t, 23)
	  }
    }
    tm.create("taskType", 24)
  }
  
  val f2 = future {
  
    for(i <- 0 to 100) {
	  q2.poll(100, "consumerIdentifier") map { t =>
	    println(s"q2 Got one! ${t.id} ${t.createTime.getTime}")
	    q2.push(t, 22)
	  }
    }
    tm.create("taskType", 24)
  }
  
  Await.result(f1, Duration.Inf)
  Await.result(f2, Duration.Inf)
  Await.result(f3, Duration.Inf)*/
  
  tm.close
}