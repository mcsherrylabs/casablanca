package sss.casablanca.task

trait TaskHandlerFactoryFactory {
  def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T]
  val supportedFactories: List[TaskHandlerFactory]
  def getHandler(taskType: String, status: TaskStatus): Option[TaskHandler]
}

object TaskHandlerFactoryFactory {
  def apply(factories: TaskHandlerFactory*): TaskHandlerFactoryFactory = new TaskHandlerFactoryFactory {

    val supportedFactories: List[TaskHandlerFactory] = factories.toList

    def getTaskFactory[T <: TaskHandlerFactory](taskType: String): Option[T] = {
      factories.find(tf => tf.getTaskType == taskType).map(_.asInstanceOf[T])
    }

    def getHandler(taskType: String, status: TaskStatus): Option[TaskHandler] = {
      val f = supportedFactories.find(tf => tf.getTaskType == taskType)
      f.flatMap(_.getHandler(status))
    }
  }

}
