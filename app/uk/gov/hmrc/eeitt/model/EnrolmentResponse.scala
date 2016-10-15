package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentResponse(ok: Boolean, response: Option[String])

object EnrolmentResponseOk extends EnrolmentResponse(true, None)

object EnrolmentResponseNotFound extends EnrolmentResponse(false, Some("not found"))

object RegisteredForDifferentFormType extends EnrolmentResponse(false, Some("registered for different form type"))

object LookupProblem extends EnrolmentResponse(false, Some("lookup problem"))

object IncorrectRequest extends EnrolmentResponse(false, Some("incorrect request"))

object EnrolmentResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentResponse] = Json.format[EnrolmentResponse]
}
