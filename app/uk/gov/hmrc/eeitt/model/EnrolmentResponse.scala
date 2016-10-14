package uk.gov.hmrc.eeitt.model

case class EnrolmentResponse(ok:Boolean, response: Option[String])

object EnrolmentResponseNotFound extends EnrolmentResponse(false, Some("not found"))

object EnrolmentResponseOk extends EnrolmentResponse(true, None)

object EnrolmentResponseMultipleFound extends EnrolmentResponse(false, Some("more than one record found"))

object EnrolmentResponseLookupProblem extends EnrolmentResponse(false, Some("lookup problem"))
