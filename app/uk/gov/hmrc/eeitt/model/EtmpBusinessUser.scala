package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class EtmpBusinessUser(registrationNumber: String, postcode: String)

object EtmpBusinessUser {
  implicit val format = Json.format[EtmpBusinessUser]
}

case class EtmpAgent(arn: String)

object EtmpAgent {
  implicit val format = Json.format[EtmpAgent]
}

