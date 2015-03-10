package casablanca.task

import casablanca.util.Logging

class TaskFatalError(msg: String) extends Error(msg) with Logging {
  log.error(msg)
}