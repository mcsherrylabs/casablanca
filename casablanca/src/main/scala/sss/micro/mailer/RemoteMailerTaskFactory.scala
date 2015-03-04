package sss.micro.mailer

import casablanca.task.TaskHandlerFactory
import casablanca.task.TaskHandlerContext
import casablanca.task.Task
import casablanca.task.TaskHandler
import casablanca.task.HandlerUpdate
import courier._
import courier.Defaults._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import casablanca.task.StatusUpdate
import casablanca.task.RelativeScheduledStatusUpdate
import java.util.concurrent.TimeUnit
import scala.util.Success
import scala.util.Failure
import casablanca.task.TaskStatus
import casablanca.webservice.remotetasks.RemoteTask
import casablanca.webservice.remotetasks.RemoteTaskHandlerFactory
import com.stackmob.newman.dsl._
import com.stackmob.newman._
import scala.concurrent._
import scala.concurrent.duration._
import java.net.URL

object RemoteMailerTaskFactory extends RemoteTaskHandlerFactory {

  val remoteTask: RemoteTask = RemoteTask("mailerNode", "mailerTask")
}