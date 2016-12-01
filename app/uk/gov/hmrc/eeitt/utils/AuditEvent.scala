package uk.gov.hmrc.eeitt.utils

import uk.gov.hmrc.eeitt.MicroserviceAuditConnector
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.{ Audit, DataEvent, EventTypes }

object AuditEvent {

  val appName = "eeitt"
  val audit = Audit(appName, MicroserviceAuditConnector)

  def sendDataEvent(transactionName: String, path: String = "N/A", tags: Map[String, String] = Map.empty,
    detail: Map[String, String])(implicit hc: HeaderCarrier): Unit = {

    val event = DataEvent(appName, EventTypes.Succeeded,
      tags = hc.toAuditTags(transactionName, path) ++ tags,
      detail = hc.toAuditDetails(detail.toSeq: _*))
    audit.sendDataEvent(event)

  }

}
