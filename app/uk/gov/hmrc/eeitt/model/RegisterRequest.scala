package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

sealed trait RegisterRequest {
  def postcode: Option[Postcode]
}

case class RegisterBusinessUserRequest(groupId: GroupId, registrationNumber: RegistrationNumber, postcode: Option[Postcode]) extends RegisterRequest {
  val regimeId = RegimeId(registrationNumber.value.substring(2, 4))
}

object RegisterBusinessUserRequest {
  implicit val registerRequestFormat: Format[RegisterBusinessUserRequest] = Json.format[RegisterBusinessUserRequest]
}

case class RegisterAgentRequest(groupId: GroupId, arn: Arn, postcode: Option[Postcode]) extends RegisterRequest

object RegisterAgentRequest {
  implicit val registerAgentRequestFormat: Format[RegisterAgentRequest] = Json.format[RegisterAgentRequest]
}
