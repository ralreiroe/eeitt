package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ Arn, EtmpAgent, EtmpBusinessUser, Postcode, RegistrationNumber }
import uk.gov.hmrc.eeitt.utils.CountryCodes

object EtmpDataParser {

  def parseFileWithBusinessUsers(source: String): List[EtmpBusinessUser] = {
    source.lines
      .filter(isUserRecord)
      .map(parseBusinessUserLine)
      .toList
  }

  def parseBusinessUserLine(line: String): EtmpBusinessUser = {
    line.split(escapedDelimiter) match {
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
          RegistrationNumber(mandatory("registrationNumber", registrationNumber)),
          customerTaxRegime,
          customerTaxRegimeDescription,
          customerOrganisationType,
          customerOrganisationTypeDescription,
          optional(customerOrganisationName),
          optional(customerTitle),
          optional(customerName1),
          optional(customerName2),
          mandatoryPostcodeIfFromTheUk(line, countryCode, Postcode(postcode)),
          mandatory("countryCode", countryCode)
        )
      }
      case _ => throw unexpectedNumberOfTokensException(line)
    }
  }

  def parseFileWithAgents(source: String): List[EtmpAgent] = {
    val agentRecords = source.lines
      .filter(isAgentRecord)
      .map(parseAgentLine).toList
    agentsFromAgentRecords(agentRecords)
  }

  def parseAgentLine(line: String): EtmpAgentRecord = {
    val tokens = line.split(escapedDelimiter)
    val expectedNumberOfTokens = 23 // 23 > the unfortunate scala limit of 22 so had to make it more complex...
    val expectedPositionOfUser = 12
    if (tokens.size == expectedNumberOfTokens) {
      tokens.splitAt(expectedPositionOfUser) match {
        case (agent @ Array(
          fileType,
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
          agentCountryCode
          ), userTokens: Tokens) => {
          val mandatory = mandatoryValue(line) _
          EtmpAgentRecord(
            Arn(mandatory("arn", arn)),
            agentIdentificationType,
            agentIdentificationTypeDescription,
            agentOrganisationType,
            agentOrganisationTypeDescription,
            optional(agentOrganisationName),
            optional(agentTitle),
            optional(agentName1),
            optional(agentName2),
            mandatoryPostcodeIfFromTheUk(line, agentCountryCode, Postcode(agentPostcode)),
            mandatory("agentCountryCode", agentCountryCode),
            customer = parseBusinessUserLine {
              // reusing customer parser but had to add a file type prefix which it expects
              val customerPartOfTheAgentLine = ("fileTypePrefix" +: userTokens).mkString(delimiter)
              customerPartOfTheAgentLine
            }
          )
        }
      }
    } else {
      throw unexpectedNumberOfTokensException(line)
    }
  }

  def agentsFromAgentRecords(agentRecords: List[EtmpAgentRecord]): List[EtmpAgent] =
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
    }.toList

  val delimiter = "|"
  val escapedDelimiter = """\""" + delimiter // escaped because pipe symbol has meaning in regex which is used by the split method

  def isUserRecord(line: String): Boolean = line.startsWith("001|")

  def isAgentRecord(line: String): Boolean = line.startsWith("002|")

  type Tokens = Array[String]

  def optional(s: String) = if (s.isEmpty) None else Some(s)

  def mandatoryValue(line: String)(fieldName: String, fieldValue: String) = {
    if (fieldValue.trim.nonEmpty) fieldValue else throw LineParsingException(s"Missing $fieldName in line: $line")
  }

  def mandatoryPostcodeIfFromTheUk(line: String, countryCode: String, postcode: Postcode): Option[Postcode] = {
    if (countryCode == CountryCodes.GB && postcode.value.trim.isEmpty) {
      throw LineParsingException(s"Missing postcode for a UK entity in line: $line")
    } else {
      optional(postcode.value).map(Postcode.apply)
    }
  }

  def unexpectedNumberOfTokensException(line: String) =
    LineParsingException(s"Failed to parse due to unexpected format of line: $line")

  // This represents a single agent line as provided by ETMP. There will be as many lines for each agent
  // as many clients they have. This class is only used internally when parsing the file and all
  // further interactions are done with EtmpAgent class which represents an agent and all their customers
  // in one class
  private[services] case class EtmpAgentRecord(
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
    countryCode: String,
    customer: EtmpBusinessUser
  )

}

case class LineParsingException(msg: String) extends Exception(msg)
