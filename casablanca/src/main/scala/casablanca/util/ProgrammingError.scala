package casablanca.util

import casablanca.task.TaskFatalError
import sss.ancillary.Logging

class ProgrammingError(msg: String) extends TaskFatalError(msg) with Logging {
  log.error(msg)
}