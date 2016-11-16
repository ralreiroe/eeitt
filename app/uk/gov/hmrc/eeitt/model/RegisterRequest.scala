package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class RegisterRequest(groupId: String, registrationNumber: String, postcode: Option[String]) {
  val regimeId = registrationNumber.substring(2, 4)
}

object RegisterRequest {
  implicit val registerRequestFormat: Format[RegisterRequest] = Json.format[RegisterRequest]
}

