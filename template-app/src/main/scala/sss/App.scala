package sss

import sss.ancillary.Logging
import sss.casablanca.WorkflowManagerImpl
import sss.casablanca.task.TaskHandlerFactoryFactory
import sss.casablanca.webservice.remotetasks.RemoteTaskHandlerFactory

import sss.mailer.MailerTaskFactory

object App extends Logging {

  def main(args: Array[String]): Unit = {

    val configName = if (args.size > 0) args(0) else "main"

    val thf = TaskHandlerFactoryFactory(
      RemoteTaskHandlerFactory,
      MailerTaskFactory)

    println(s"This instance supports the following task factories and statuses...")
    thf.supportedFactories.foreach { fact =>
      println(s"${fact.getTaskType}  ${fact.getSupportedStatuses}")
    }

    val wfm = new WorkflowManagerImpl(thf, configName)
    wfm.start

  }
}
