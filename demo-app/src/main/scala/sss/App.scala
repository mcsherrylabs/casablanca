package sss

import sss.ancillary.Logging
import sss.casablanca.WorkflowManagerImpl
import sss.casablanca.task.TaskHandlerFactoryFactory
import sss.casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import sss.demo.DemoTaskFactory

object App extends Logging {

  def main(args: Array[String]): Unit = {

    val configName = if (args.size > 0) {
      args(0)
    } else "main"

    val (row: Int, col: Int) = if (args.size > 2) {
      (args(1).toInt, args(2).toInt)
    } else (0, 0)

    val dtf = new DemoTaskFactory(row, col)
    val thf = TaskHandlerFactoryFactory(
      RemoteTaskHandlerFactory,
      dtf)

    println(s"This instance supports the following task factories and statuses...")
    thf.supportedFactories.foreach { fact =>
      println(s"${fact.getTaskType}  ${fact.getSupportedStatuses}")
    }

    val wfm = new WorkflowManagerImpl(thf, configName)
    wfm.start

  }
}
