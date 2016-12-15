package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class EtmpBusinessUser(
  registrationNumber: RegistrationNumber,
  taxRegime: String,
  taxRegimeDescription: String,
  organisationType: String,
  organisationTypeDescription: String,
  organisationName: Option[String],
  customerTitle: Option[String],
  customerName1: Option[String],
  customerName2: Option[String],
  postcode: Option[Postcode],
  countryCode: Option[String]
)

object EtmpBusinessUser {
  implicit val format = Json.format[EtmpBusinessUser]
}

case class EtmpAgent(
  arn: Arn,
  identificationType: String,
  identificationTypeDescription: String,
  organisationType: String,
  organisationTypeDescription: String,
  organisationName: Option[String],
  title: Option[String],
  name1: Option[String],
  name2: Option[String],
  postcode: Option[Postcode],
  countryCode: Option[String],
  customers: Seq[EtmpBusinessUser]
)

object EtmpAgent {
  implicit val format = Json.format[EtmpAgent]
}
