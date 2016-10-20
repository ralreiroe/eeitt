package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentVerificationResponse(error: Option[String])

object EnrolmentVerificationResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentVerificationResponse] = Json.format[EnrolmentVerificationResponse]
  val RESPONSE_OK = None
  val RESPONSE_NOT_FOUND = Some("not found")
  val INCORRECT_REGIME = Some("incorrect regime")
  val INCORRECT_POSTCODE = Some("incorrect postcode")
  val INCORRECT_ARN = Some("incorrect ARN")
  val MISSING_ARN = Some("missing ARN")
  val INCORRECT_ARN_FOR_CLIENT = Some("incorrect ARN for client")
}
