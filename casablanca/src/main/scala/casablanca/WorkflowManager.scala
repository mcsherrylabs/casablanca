package casablanca

import casablanca.task.Task
import casablanca.task.HandlerUpdate
import casablanca.task.TaskHandler
import casablanca.queues.StatusQueueManager
import casablanca.queues.Scheduler
import casablanca.task.TaskManager
import casablanca.task.TaskHandlerFactory
import java.util.Date
import casablanca.task.TaskHandlerFactoryFactory
import casablanca.queues.StatusQueue
import casablanca.queues.StatusQueueWorker
import casablanca.webservice.RestServer
import casablanca.util.Logging
import scala.concurrent.Future

/**
 *
 */
trait WorkflowManager {

  def start
  def stop
}

class WorkflowManagerImpl(tm: TaskManager,
  statusQManager: StatusQueueManager,
  statusHandlerFactory: TaskHandlerFactoryFactory,
  scheduler: Scheduler,
  restServer: RestServer) extends WorkflowManager with Logging {

  Runtime.getRuntime().addShutdownHook(new Thread() {

    override def run {
      log.info("Casablanca shutting down ... ")
      try {
        WorkflowManagerImpl.this.stop
      } catch {
        case e: Exception => log.error("Casablanca shut down error ... ", e)
      }
      log.info("Casablanca shut down complete ... ")
    }
  })

  log.info("Shutdown hook installed ... ")

  def stop {
    // todo add other stops for threads
    tm.close
  }

  def start {

    val when = new Date().getTime
    log.info("Starting casablanca ... ")
    val statusQueues = statusQManager.statusQueues
    statusQueues.map(q => q.init)
    val workerQueue = new java.util.concurrent.ArrayBlockingQueue[StatusQueue](statusQueues.size)
    statusQueues.foreach { e => workerQueue.put(e) }

    import scala.concurrent.ExecutionContext.Implicits.global

    Future { restServer.start }
    log.info("Started REST server ... ")

    for (i <- 0 to 5) {
      new StatusQueueWorker(workerQueue).start
      log.info(s"Started task queue worker # ${i + 1} ... ")
    }
    scheduler.start
    log.info("Started scheduler ... ")
    val later = new Date().getTime
    log.info(s"Casablanca startup sucessful in ${later - when} ms !")
  }

}