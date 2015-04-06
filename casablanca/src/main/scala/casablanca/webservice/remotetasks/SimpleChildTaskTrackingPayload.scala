package casablanca.webservice.remotetasks

case class SimpleChildTaskTrackingPayload(payload: String, val remoteTasks: Map[String, String]) extends ChildTaskTrackingPayload