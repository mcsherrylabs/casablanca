package casablanca.util

import casablanca.task.TaskFatalError

class ProgrammingError(msg: String) extends TaskFatalError(msg) with Logging {
  log.error(msg)
}