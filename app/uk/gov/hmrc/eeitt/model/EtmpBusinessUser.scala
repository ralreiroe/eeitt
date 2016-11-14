package uk.gov.hmrc.eeitt.model

import play.api.libs.json._
import uk.gov.hmrc.eeitt.services.LineParsingException

//case class EtmpBusinessUser(registrationNumber: String, postcode: String)

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

object Test {

  val agent = "foo".split("") match {
    case Array(
      agentReferenceNumber,
      agentIdentificationType,
      agentIdentificationTypeDescription,
      agentOrganisationType,
      agentOrganisationTypeDescription,
      agentOrganisationName,
      agentTitle,
      agentName1,
      agentName2,
      agentPostcode,
      agentCountryCode,
      customerIdentificationNumber,
      customerTaxRegime,
      customerTaxRegimeDescription,
      customerOrganisationType,
      customerOrganisationTypeDescription,
      customerOrganisationName,
      customerCustomerTitle,
      customerCustomerName1,
      customerCustomerName2,
      customerPostcode,
      customerCountryCode
      ) =>
      EtmpAgentRecord(
        mandatory(agentReferenceNumber),
        agentIdentificationType,
        agentIdentificationTypeDescription,
        agentOrganisationType,
        agentOrganisationTypeDescription,
        optional(agentOrganisationName),
        optional(agentTitle),
        optional(agentName1),
        optional(agentName2),
        optional(agentPostcode),
        mandatory(agentCountryCode),
        customer = EtmpBusinessUser(
          mandatory(customerIdentificationNumber),
          customerTaxRegime,
          customerTaxRegimeDescription,
          customerOrganisationType,
          customerOrganisationTypeDescription,
          optional(customerOrganisationName),
          optional(customerCustomerTitle),
          optional(customerCustomerName1),
          optional(customerCustomerName2),
          optional(customerPostcode),
          mandatory(customerCountryCode)
        )
      )
  }

  def optional(s: String) = if (s.isEmpty) None else Some(s)
  def mandatory(s: String) = if (s.nonEmpty) s else throw LineParsingException(s)

}

case class EtmpAgentRecord(
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
  customer: EtmpBusinessUser
)

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
  def fromRecords(agentRecords: Seq[EtmpAgentRecord]): Seq[EtmpAgent] = {
    agentRecords.groupBy(_.arn).map {
      case (agentArn, records) =>
        val agentData = records.head
        import agentData._
        EtmpAgent(
          arn,
          identificationType,
          identificationTypeDescription,
          organisationType,
          organisationTypeDescription,
          organisationName,
          title,
          name1,
          name2,
          postcode,
          countryCode,
          customers = records.map(_.customer)
        )
    }
  }.toList
  implicit val format = Json.format[EtmpAgent]
}

