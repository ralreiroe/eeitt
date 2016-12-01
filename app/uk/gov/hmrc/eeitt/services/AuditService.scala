package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.MicroserviceAuditConnector
import uk.gov.hmrc.eeitt.model.{ Postcode, RegisterAgentRequest, RegisterBusinessUserRequest, RegisterRequest }
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.{ Audit, DataEvent, EventTypes }
import uk.gov.hmrc.play.http.HeaderCarrier

object AuditService {

  val appName = "eeitt"
  val audit = Audit(appName, MicroserviceAuditConnector)

  def sendRegisteredBusinessUserEvent(path: String, request: RegisterBusinessUserRequest, tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = {
    val requestTags = Map {
      "registration-number" -> request.registrationNumber.value
      "group-id" -> request.groupId.value
      "regime-id" -> request.regimeId.value
    }
    sendRegisteredEvent(path, request.postcode, tags ++ requestTags)
  }

  def sendRegisteredAgentEvent(path: String, request: RegisterAgentRequest, tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = {
    val requestTags = Map[String, String] {
      "arn" -> request.arn.value
      "group-id" -> request.groupId.value
    }
    sendRegisteredEvent(path, request.postcode, tags ++ requestTags)
  }

  def sendRegisteredEvent(path: String, postcode: Option[Postcode], tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = {
    val postcodeTag = postcode.map {
      case p => ("postcode", p.value)
    }
    sendDataEvent("registered", path, tags ++ postcodeTag, Map.empty)
  }

  def sendDataLoadEvent(path: String, tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = {
    sendDataEvent("data-loaded", path, tags, Map.empty)
  }

  private def sendDataEvent(transactionName: String, path: String = "N/A", tags: Map[String, String] = Map.empty,
    detail: Map[String, String])(implicit hc: HeaderCarrier): Unit = {
    val event = DataEvent(appName, EventTypes.Succeeded,
      tags = hc.toAuditTags(transactionName, path) ++ tags,
      detail = hc.toAuditDetails(detail.toSeq: _*))
    audit.sendDataEvent(event)
  }

}
