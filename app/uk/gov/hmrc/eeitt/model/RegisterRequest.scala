package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class RegisterRequest(groupId: String, registrationNumber: String, postcode: String) {
//   todo substring may fail, option would be better here or sth like that
  val regimeId = registrationNumber.substring(2,4)
}

object RegisterRequest {
  implicit val registerRequestFormat: Format[RegisterRequest] = Json.format[RegisterRequest]
}

