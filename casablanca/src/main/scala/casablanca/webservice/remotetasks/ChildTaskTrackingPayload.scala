package casablanca.webservice.remotetasks

trait ChildTaskTrackingPayload {
  val payload: String
  val remoteTasks: Map[String, String]
}
