package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class RegisterRequest(groupId: GroupId, registrationNumber: RegistrationNumber, postcode: String) {
  val regimeId = RegimeId(registrationNumber.value.substring(2, 4))
}

object RegisterRequest {
  implicit val registerRequestFormat: Format[RegisterRequest] = Json.format[RegisterRequest]
}
