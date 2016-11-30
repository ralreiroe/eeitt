package uk.gov.hmrc.eeitt.utils

object AuditEvent {
  import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
  import uk.gov.hmrc.play.audit.http.connector.AuditConnector
  import uk.gov.hmrc.play.config.RunMode
  import uk.gov.hmrc.play.http.HeaderCarrier
  import uk.gov.hmrc.play.audit.AuditExtensions._
  import uk.gov.hmrc.play.audit.model.{ Audit, DataEvent, EventTypes }

  val appName = "eeitt"
  val audit = Audit(
    appName,
    new AuditConnector with RunMode {
      override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
    }
  )

  def sendDataEvent(transactionName: String, path: String = "N/A", tags: Map[String, String] = Map.empty,
    detail: Map[String, String])(implicit hc: HeaderCarrier): Unit = {

    audit.sendDataEvent(DataEvent(appName, EventTypes.Succeeded,
      tags = hc.toAuditTags(transactionName, path) ++ tags,
      detail = hc.toAuditDetails(detail.toSeq: _*)))

  }

}
