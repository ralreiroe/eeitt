package uk.gov.hmrc.eeitt.model
import play.api.libs.json._

case class EnrolmentVerificationRequest(formTypeRef: String, registrationNumber: String, livesInTheUk: Boolean, postcode: String, isAgent: Boolean, arn: String)

object EnrolmentVerificationRequest {
  implicit val enrolmentVerificationRequestFormat: Format[EnrolmentVerificationRequest] = Json.format[EnrolmentVerificationRequest]
}

