package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentVerificationResponse(error: Option[String])

object ResponseOk extends EnrolmentVerificationResponse(None)
object ResponseNotFound extends EnrolmentVerificationResponse(Some("not found"))
object RegisteredForDifferentFormType extends EnrolmentVerificationResponse(Some("registered for different form type"))

object EnrolmentVerificationResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentVerificationResponse] = Json.format[EnrolmentVerificationResponse]
}
