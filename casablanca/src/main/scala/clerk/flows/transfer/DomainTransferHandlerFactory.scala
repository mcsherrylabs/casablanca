package clerk.flows.transfer

import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandler
import casablanca.task.Task
import casablanca.task.TaskImpl
import casablanca.db.Row
import casablanca.task.TaskManager
import casablanca.task.BaseTask
import casablanca.task.TaskHandlerContext
import java.util.Date
import casablanca.task.TaskDescriptor

/**
 * This will become a real boy ...
 */
class DomainTransferTask(t: Task) extends BaseTask(t)

object DomainTransferConsts {
  val domainTransferTaskType = "domainTransfer"
  val initialiseTransfer = 1
  val informOwner = 2
  val awaitOwnerReponse = 3
  val updateRegistry = 4
  val acceptTransfer = 5
  val rejectTransfer = 6
  val cancelTransfer = 7
  val transferTaskComplete = 8
}

object DomainTransferHandlerFactory extends TaskHandlerFactory {

  import DomainTransferConsts._

  def getSupportedStatuses: Set[Int] = {
    Set(initialiseTransfer, updateRegistry, rejectTransfer, awaitOwnerReponse, acceptTransfer)
  }

  def getTaskType: String = domainTransferTaskType

  def getHandler[T >: TaskHandler](status: Int) = {

    status match {
      case DomainTransferConsts.initialiseTransfer => Some(new InformOwnerHandler())
      case DomainTransferConsts.updateRegistry => Some(new UpdateRegistryHandler())
      case DomainTransferConsts.awaitOwnerReponse => Some(new GetResponseHandler())
      case DomainTransferConsts.acceptTransfer => Some(new FinaliseTransferHandler())
      case DomainTransferConsts.rejectTransfer => Some(new RejectTransferHandler())
      case _ => None
    }

  }

  def createInitTask(taskHandlerContext: TaskHandlerContext, domainName: String, aspirantId: String, ownerId: String): DomainTransferTask = {
    new DomainTransferTask(taskHandlerContext.startTask(
      TaskDescriptor(domainTransferTaskType, initialiseTransfer, Seq(domainName, aspirantId, ownerId).mkString(","))))
  }
}

