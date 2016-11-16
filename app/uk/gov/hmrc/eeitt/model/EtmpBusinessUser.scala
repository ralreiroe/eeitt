package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class EtmpBusinessUser(registrationNumber: RegistrationNumber, postcode: String)

object EtmpBusinessUser {
  implicit val format = Json.format[EtmpBusinessUser]
}

case class EtmpAgent(arn: Arn)

object EtmpAgent {
  implicit val format = Json.format[EtmpAgent]
}
