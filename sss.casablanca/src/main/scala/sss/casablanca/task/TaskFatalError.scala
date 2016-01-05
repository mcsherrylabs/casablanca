package sss.casablanca.task

import _root_.sss.ancillary.Logging

class TaskFatalError(msg: String) extends Error(msg) with Logging {
  log.error(msg)
}