package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentResponse(error: Option[String])

object EnrolmentResponseOk extends EnrolmentResponse(None)

object EnrolmentResponseNotFound extends EnrolmentResponse(Some("not found"))

object RegisteredForDifferentFormType extends EnrolmentResponse(Some("registered for different form type"))

object LookupProblem extends EnrolmentResponse(Some("lookup problem"))

object IncorrectRequest extends EnrolmentResponse(Some("incorrect request"))

object EnrolmentResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentResponse] = Json.format[EnrolmentResponse]
}
