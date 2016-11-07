package uk.gov.hmrc.eeitt.model
import play.api.libs.json._

case class RegisterRequest(groupId: String, regimeId: String, registrationNumber: String, postcode: String)

object RegisterRequest {
  implicit val registerRequestFormat: Format[RegisterRequest] = Json.format[RegisterRequest]
}

