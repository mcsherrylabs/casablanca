package clerk.flows.transfer

import casablanca.task.BaseTaskHandlerFactory
import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.TaskImpl
import casablanca.db.Row
import casablanca.task.TaskManager
import casablanca.task.BaseTask
import casablanca.task.TaskHandlerContext
import java.util.Date
import casablanca.task.TaskDescriptor
import casablanca.task.TaskStatus
import casablanca.task.TaskStatuses
import casablanca.task.StatusUpdate
import casablanca.task.HandlerUpdate

/**
 * This will become a real boy ...
 */
class DomainTransferTask(t: Task) extends BaseTask(t)

object DomainTransferConsts extends TaskStatuses {

  val initialiseTransfer = TaskStatus(1000)
  val informOwner = TaskStatus(2000)
  val awaitOwnerReponse = TaskStatus(3000)
  val updateRegistry = TaskStatus(4000)
  val acceptTransfer = TaskStatus(5000)
  val rejectTransfer = TaskStatus(6000)
  val cancelTransfer = TaskStatus(7000)
  val transferTaskComplete = TaskStatus(8000)
}

object DomainTransferHandlerFactory extends BaseTaskHandlerFactory {

  import DomainTransferConsts._

  override def getSupportedStatuses: Set[TaskStatus] = {
    Set(initialiseTransfer, updateRegistry, rejectTransfer, awaitOwnerReponse, acceptTransfer) ++ super.getSupportedStatuses
  }

  def getTaskType: String = "domainTransfer"

  override def getHandler[T >: TaskHandler](status: TaskStatus) = {

    status match {
      case DomainTransferConsts.initialiseTransfer => Some(new InformOwnerHandler())
      case DomainTransferConsts.updateRegistry => Some(new UpdateRegistryHandler())
      case DomainTransferConsts.awaitOwnerReponse => Some(new GetResponseHandler())
      case DomainTransferConsts.acceptTransfer => Some(new FinaliseTransferHandler())
      case DomainTransferConsts.rejectTransfer => Some(new RejectTransferHandler())
      case _ => super.getHandler(status)
    }

  }

  def createInitTask(taskHandlerContext: TaskHandlerContext, domainName: String, aspirantId: String, ownerId: String): DomainTransferTask = {
    new DomainTransferTask(taskHandlerContext.startTask(
      TaskDescriptor(getTaskType, initialiseTransfer, Seq(domainName, aspirantId, ownerId).mkString(","))))
  }

  override def consume(taskContext: TaskHandlerContext, task: Task, event: String): Option[HandlerUpdate] = {
    println(s"Looks like ${task}")
    None
  }
}

