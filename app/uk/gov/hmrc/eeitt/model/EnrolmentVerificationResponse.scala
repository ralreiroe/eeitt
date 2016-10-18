package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentVerificationResponse(error: Option[String])

object EnrolmentVerificationResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentVerificationResponse] = Json.format[EnrolmentVerificationResponse]
  val RESPONSE_OK = None
  val RESPONSE_NOT_FOUND = Some("not found")
  val RESPONSE_DIFFERENT_FORM_TYPE = Some("registered for different form type")
}
