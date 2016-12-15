package uk.gov.hmrc.eeitt.typeclasses

import org.scalatest._
import uk.gov.hmrc.eeitt.model.{ AuditData, EtmpAgent, EtmpBusinessUser, Postcode }
import uk.gov.hmrc.eeitt.services.{ AuditService, HmrcAuditService }
import uk.gov.hmrc.play.http.HeaderCarrier

class HmrcAuditSpec extends FlatSpec with Matchers with OptionValues {
  "HmrcAuditSpec" should "prepare audit data for EtmpAgent" in {

    implicit val etmpAgentRepo = new HmrcAuditService {
      override def sendRegisteredEvent(path: String, postcode: Option[Postcode], tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = {
        path should be("/path")
        postcode.value.value should be("123")
        tags should contain("user-type" -> "agent")
        ()
      }

      override def sendDataLoadEvent(path: String, tags: Map[String, String] = Map.empty)(implicit hc: HeaderCarrier): Unit = ()
    }

    val auditData = new AuditData("/path", Some(Postcode("123")), Map("user-type" -> "agent"))
    implicitly[HmrcAudit[AuditData]].apply(auditData)(new HeaderCarrier())
  }
}
