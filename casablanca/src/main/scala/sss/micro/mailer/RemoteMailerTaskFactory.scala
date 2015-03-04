package sss.micro.mailer

import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory

object RemoteMailerTaskFactory extends RemoteTaskHandlerFactory {
  val remoteTask: RemoteTask = RemoteTask("mailerNode", "mailerTask")
}