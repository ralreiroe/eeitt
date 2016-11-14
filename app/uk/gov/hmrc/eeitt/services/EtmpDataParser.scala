package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpAgentRecord, EtmpBusinessUser }

/*
    todo: add more constraints once we know more about the data:
    - optionality of fields?
    - expected number of chars?
    - non-empty values?
    - validate postcode?
    - use ScalaCheck to test all that?
 */
trait EtmpDataParser[A] {
  // escaped because literal | has meaning in regex
  def delimiter = "\\|"

  def isRecord(line: String): Boolean

  type Tokens = Array[String]

  def extractDataFromTokens(line: String): PartialFunction[Tokens, A]

  def parseLine(line: String): A = {
    extractDataFromTokens(line).applyOrElse(
      line.split(delimiter),
      (_: Tokens) => throw LineParsingException(s"Failed to parse due to unexpected format of line: $line")
    )
  }

  def optional(s: String) = if (s.isEmpty) None else Some(s)

  def mandatoryValue(line: String)(fieldName: String, fieldValue: String) = {
    if (fieldValue.trim.nonEmpty) fieldValue else throw LineParsingException(s"Missing $fieldName in line: $line")
  }

  def mandatoryPostcodeIfFromGB(line: String, countryCode: String, postcode: String): Option[String] = {
    if (countryCode == "GB" && postcode.trim.isEmpty) {
      throw LineParsingException(s"Missing postcode for a UK entity in line: $line")
    } else {
      optional(postcode)
    }
  }

  def parseFile(source: String): List[A] = {
    source.lines
      .filter(isRecord)
      .map(parseLine)
      .toList
  }
}

case class LineParsingException(msg: String) extends Exception(msg)

/*
 *  Imported File from ETMP has following structure for Business Users:
 *  - header line: 00|CUSTOMER_DATA|ETMP|MDTP|DATE_OF_EXPORT|TIME_OF_EXPORT
 *  - records with a structure like: 001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
 *    where tokens delimited by the pipe | character are as follows:
 *      - File Type
 *      - Customer Identification Number (aka registration number)
 *      - Customer Tax regime
 *      - Customer Organisation Type
 *      - Customer Organisation Type Description
 *      - Customer Organisation Name
 *      - Customer Title
 *      - Customer Name1
 *      - Customer Name2
 *      - Customer Postal Code
 *      - Customer Country Code
 *  - EOF line: 99|CUSTOMER_DATA|NUMBER_OF_RECORDS
 */
object EtmpBusinessUserParser extends EtmpDataParser[EtmpBusinessUser] {
  def extractDataFromTokens(line: String) = {
    case Array(
      fileType,
      registrationNumber,
      customerTaxRegime,
      customerTaxRegimeDescription,
      customerOrganisationType,
      customerOrganisationTypeDescription,
      customerOrganisationName,
      customerTitle,
      customerName1,
      customerName2,
      postcode,
      countryCode
      ) => {
      val mandatory = mandatoryValue(line) _
      EtmpBusinessUser(
        mandatory("registrationNumber", registrationNumber),
        customerTaxRegime,
        customerTaxRegimeDescription,
        customerOrganisationType,
        customerOrganisationTypeDescription,
        optional(customerOrganisationName),
        optional(customerTitle),
        optional(customerName1),
        optional(customerName2),
        mandatoryPostcodeIfFromGB(line, countryCode, postcode),
        mandatory("countryCode", countryCode)
      )
    }
  }

  def isRecord(line: String): Boolean = line.startsWith("001|")
}

/*
 *  Imported File from ETMP has following structure for Agents:
 *  - header line: 00|AGENT_DATA|ETMP|MDTP|DATE_OF_EXPORT|TIME_OF_EXPORT
 *  - records with following structure: 002|KARN0000086|ARN|Agent1||||BN12 4XL|XMAP00000100051|ZAPD|Organisation1|||
 *    where tokens are as follows:
 *      - File Type
 *      - Agent Reference Number
 *      - Agent Reference Type
 *      - Agent Organisation Name
 *      - Agent Title
 *      - Agent Name1
 *      - Agent Name2
 *      - Agent Postal Code
 *      - Customer Reference Number
 *      - Customer Tax regime
 *      - Customer Organisation Name
 *      - Customer Title
 *      - Customer Name1
 *      - Customer Name2
 *  - EOF line: 99|AGENT_DATA|NUMBER_OF_RECORDS
 */
object EtmpAgentRecordParser extends EtmpDataParser[EtmpAgentRecord] {
  def extractDataFromTokens(line: String) = {
    case Array(
      arn,
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
      ) => {
      val mandatory = mandatoryValue(line) _
      EtmpAgentRecord(
        mandatory("arn", arn),
        agentIdentificationType,
        agentIdentificationTypeDescription,
        agentOrganisationType,
        agentOrganisationTypeDescription,
        optional(agentOrganisationName),
        optional(agentTitle),
        optional(agentName1),
        optional(agentName2),
        mandatoryPostcodeIfFromGB(line, agentCountryCode, agentPostcode),
        mandatory("agentCountryCode", agentCountryCode),
        customer = EtmpBusinessUser(
          mandatory("customerIdentificationNumber", customerIdentificationNumber),
          customerTaxRegime,
          customerTaxRegimeDescription,
          customerOrganisationType,
          customerOrganisationTypeDescription,
          optional(customerOrganisationName),
          optional(customerCustomerTitle),
          optional(customerCustomerName1),
          optional(customerCustomerName2),
          mandatoryPostcodeIfFromGB(line, customerPostcode, customerCountryCode),
          mandatory("customerCountryCode", customerCountryCode)
        )
      )
    }
  }

  def isRecord(line: String): Boolean = line.startsWith("002|")
}

