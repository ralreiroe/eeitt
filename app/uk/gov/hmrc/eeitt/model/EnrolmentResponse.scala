package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentResponse(ok: Boolean, response: Option[String])

object EnrolmentResponseNotFound extends EnrolmentResponse(false, Some("not found"))

object EnrolmentResponseOk extends EnrolmentResponse(true, None)

object MultipleFound extends EnrolmentResponse(false, Some("more than one record found"))

object LookupProblem extends EnrolmentResponse(false, Some("lookup problem"))

object IncorrectRequest extends EnrolmentResponse(false, Some("incorrect request"))

object EnrolmentResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentResponse] = Json.format[EnrolmentResponse]
}
