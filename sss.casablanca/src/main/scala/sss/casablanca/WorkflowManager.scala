package sss.casablanca

import java.util.Date
import java.util.concurrent.Executors

import _root_.sss.ancillary.{ Configure, Logging }
import _root_.sss.casablanca.queues._
import _root_.sss.casablanca.task.{ TaskManager, TaskHandlerFactoryFactory }
import _root_.sss.casablanca.webservice.{ RestServer, Visibility, Endpoint, TaskCompletionListener }
import _root_.sss.casablanca.webservice.remotetasks.NodeConfig
import _root_.sss.db.Db
import com.twitter.finatra.Controller

import scala.concurrent.Future

/**
 *
 */
trait WorkflowManager {
  def start
  def stop
}

class WorkflowManagerImpl(taskHandlerFactoryFactory: TaskHandlerFactoryFactory,
  configName: String = "main",
  userControllerList: List[Controller] = Nil,
  dbOpt: Option[Db] = None) extends WorkflowManager
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

  val tm = dbOpt match {
    case Some(db) => new TaskManager(db, config.getConfig(configName))
    case None => new TaskManager(config.getConfig(configName))
  }
  val nodeConfig = new NodeConfig(config.getConfig(configName))

  val scheduledExecutorService = Executors.newScheduledThreadPool(1)
  val taskCompletionschedulerPool = Executors.newScheduledThreadPool(config.getInt("taskCompletionSchedulerPoolSize"))
  val taskCompletionListener = new TaskCompletionListener()(taskCompletionschedulerPool)
  val statusQManager = new StatusQueueManager(tm, taskHandlerFactoryFactory, nodeConfig, taskCompletionListener)
  val scheduler = new Scheduler(tm, scheduledExecutorService, statusQManager, config.getInt("schedulerGranularityInSeconds"))
  val reaper = new Reaper(tm, scheduledExecutorService, config.getInt("reaperGranularityInSeconds"), config.getInt("waitBeforeDeletingInMinutes"))
  val controllerList = List(new Endpoint(statusQManager.taskContext, taskCompletionListener),
    new Visibility(tm)) ++ userControllerList
  val restServer = new RestServer(config.getConfig(configName),
    controllerList)

  def stop {
    // todo add other stops for threads
    tm.close
  }

  def start {

    val numWorkers = config.getInt("sizeWorkerThreadPool")
    val when = new Date().getTime
    log.info("Starting sss.casablanca ... ")
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
    log.info("Started scheduler ... ")
    reaper.start
    log.info("Started reaper ... ")
    val later = new Date().getTime
    log.info(s"Casablanca startup successful in ${later - when} ms !")
  }

}