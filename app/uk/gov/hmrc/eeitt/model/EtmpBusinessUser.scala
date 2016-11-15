package uk.gov.hmrc.eeitt.model

import play.api.libs.json._

case class EtmpBusinessUser(
  registrationNumber: String,
  taxRegime: String,
  taxRegimeDescription: String,
  organisationType: String,
  organisationTypeDescription: String,
  organisationName: Option[String],
  customerTitle: Option[String],
  customerName1: Option[String],
  customerName2: Option[String],
  postcode: Option[String],
  countryCode: String
)

object EtmpBusinessUser {
  implicit val format = Json.format[EtmpBusinessUser]
}

case class EtmpAgent(
  arn: String,
  identificationType: String,
  identificationTypeDescription: String,
  organisationType: String,
  organisationTypeDescription: String,
  organisationName: Option[String],
  title: Option[String],
  name1: Option[String],
  name2: Option[String],
  postcode: Option[String],
  countryCode: String,
  customers: Seq[EtmpBusinessUser]
)

object EtmpAgent {
  implicit val format = Json.format[EtmpAgent]
}

