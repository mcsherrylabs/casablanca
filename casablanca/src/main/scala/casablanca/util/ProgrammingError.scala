package casablanca.util

class ProgrammingError(msg: String) extends Error(msg) with Logging {
  log.error(msg)
}