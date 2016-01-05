package sss.casablanca.task

import _root_.sss.ancillary.{ Configure, Logging }
import sss.casablanca.util.Lockable
import sss.casablanca.webservice.remotetasks.TaskDoneHandler

trait BaseTaskHandlerFactory extends TaskHandlerFactory
    with Lockable[String]
    with Logging
    with Configure {

  protected lazy val taskConfig = {
    if (config.hasPath(getTaskType)) Some(config.getConfig(getTaskType))
    else None
  }

  def getStatusConfig(status: Int): StatusConfig = {
    //val configPath = s"${getTaskType}.${status}"
    taskConfig match {
      case Some(someConfig) if (someConfig.hasPath(s"${status}")) => {
        val conf = someConfig.getConfig(s"${status}")
        log.info(s"Using task specific settings for ${getTaskType}:${status}")
        StatusConfig(
          conf.getInt("queueSize"),
          conf.getInt("offerTimeoutMs"),
          conf.getInt("pollTimeoutMs"),
          conf.getInt("maxRetryCount"),
          conf.getInt("retryDelayMinutes"))
      }
      case _ => StatusConfig()
    }
  }

  override def getSupportedStatuses: Set[TaskStatus] = Set(taskFinished, taskFailed)

  override def getHandler[T >: TaskHandler](status: TaskStatus): Option[T] = {
    status match {
      case `taskFinished` => Some(TaskDoneHandler)
      case `taskFailed` => Some(TaskDoneHandler)
      case x => {
        log.warn(s"No handler found for status ${x} in Base Factory ")
        None
      }
    }
  }

  def handleEvent(taskContext: TaskHandlerContext, task: Task, ev: TaskEvent) {

    log.debug(s"Consuming event from ${ev.origin}")
    log.debug(s"Consuming event payload ${ev.eventPayload}")
    doLocked[Option[HandlerUpdate]](task.id, () => consume(taskContext, task, ev)) map { up => taskContext.pushTask(task, up) }
  }

  def consume(taskContext: TaskHandlerContext, task: Task, event: TaskEvent): Option[HandlerUpdate] = {
    throw new UnsupportedOperationException("One must override 'consume' in order to consume events.")
  }

}

