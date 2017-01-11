package uk.gov.hmrc.eeitt.typeclasses

import uk.gov.hmrc.eeitt.services.AuditService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.eeitt.model.AuditData

trait HmrcAudit[A] {
  def apply(a: A): HeaderCarrier => Unit
}

object HmrcAudit {
  implicit def hmrcAudit(implicit auditService: AuditService): HmrcAudit[AuditData] = {
    new HmrcAudit[AuditData] {
      override def apply(ad: AuditData): HeaderCarrier => Unit = implicit hc => {
        auditService.sendRegisteredEvent(ad.path, ad.postcode, ad.tags)
      }
    }
  }
}
