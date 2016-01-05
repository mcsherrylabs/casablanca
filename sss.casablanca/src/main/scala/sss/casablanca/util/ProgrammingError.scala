package sss.casablanca.util

import sss.ancillary.Logging
import sss.casablanca.task.TaskFatalError

class ProgrammingError(msg: String) extends TaskFatalError(msg) with Logging {
  log.error(msg)
}