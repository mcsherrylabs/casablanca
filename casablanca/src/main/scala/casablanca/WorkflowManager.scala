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
import casablanca.util.Configure
import com.twitter.finatra.Controller
import casablanca.webservice.Endpoint
import casablanca.webservice.remotetasks.NodeConfig
import casablanca.queues.Reaper
import casablanca.webservice.Visibility

/**
 *
 */
trait WorkflowManager {
  def start
  def stop
}

class WorkflowManagerImpl(taskHandlerFactoryFactory: TaskHandlerFactoryFactory,
    configName: String = "main") extends WorkflowManager   
  with Logging 
  with Configure {

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

  val tm = new TaskManager(config.getConfig(configName))
  val nodeConfig = new NodeConfig(config.getConfig(configName)) 
  val statusQManager = new StatusQueueManager(tm, taskHandlerFactoryFactory, nodeConfig)
  val scheduler = new Scheduler(tm, statusQManager, config.getInt("schedulerGranularityInSeconds"))
  val reaper = new Reaper(tm, config.getInt("reaperGranularityInSeconds"), config.getInt("waitBeforeDeletingInMinutes"))
  val restServer = new RestServer(config.getConfig(configName), 
      new Endpoint(statusQManager.taskContext),
      new Visibility(tm))

  def stop {
    // todo add other stops for threads
    tm.close
  }

  def start {

    val numWorkers = config.getInt("sizeWorkerThreadPool")
    val when = new Date().getTime
    log.info("Starting casablanca ... ")
    val statusQueues = statusQManager.statusQueues
    statusQueues.map(q => q.init)
    val workerQueue = new java.util.concurrent.ArrayBlockingQueue[StatusQueue](statusQueues.size)
    statusQueues.foreach { e => workerQueue.put(e) }

    import scala.concurrent.ExecutionContext.Implicits.global

    Future { restServer.start }
    log.info("Started REST server ... ")

    for (i <- 1 to numWorkers) {
      new StatusQueueWorker(workerQueue).start
      log.info(s"Started task queue worker # ${i} ... ")
    }
    scheduler.start
    reaper.start
    log.info("Started scheduler ... ")
    val later = new Date().getTime
    log.info(s"Casablanca startup sucessful in ${later - when} ms !")
  }

}