package uk.gov.hmrc.eeitt

import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{ AppName, RunMode, ServicesConfig }
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import play.api.libs.ws.WSAPI
import play.api.libs.ws.WSRequest

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName {
  override val hooks: Seq[HttpHook] = NoneRequired
}

class MicroserviceAuditConnector(val wsApi: WSAPI) extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
  override def buildRequest(url: String)(implicit hc: HeaderCarrier): WSRequest = wsApi.url(url).withHeaders(hc.headers: _*)
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig {
  override val authBaseUrl = baseUrl("auth")
}
