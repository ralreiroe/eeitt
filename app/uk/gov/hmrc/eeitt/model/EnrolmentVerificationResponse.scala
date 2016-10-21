package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class EnrolmentVerificationResponse(error: Option[String])

object EnrolmentVerificationResponse {
  implicit val enrolmentResponseFormat: Format[EnrolmentVerificationResponse] = Json.format[EnrolmentVerificationResponse]
  val RESPONSE_OK = this(None)
  val RESPONSE_NOT_FOUND = this(Some("not found"))
  val MULTIPLE_FOUND = this(Some("multiple found"))
  val INCORRECT_REGIME = this(Some("incorrect regime"))
  val INCORRECT_POSTCODE = this(Some("incorrect postcode"))
  val INCORRECT_ARN = this(Some("incorrect ARN"))
  val MISSING_ARN = this(Some("missing ARN"))
  val INCORRECT_ARN_FOR_CLIENT = this(Some("incorrect ARN for client"))
}
