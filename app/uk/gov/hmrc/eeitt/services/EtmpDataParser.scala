package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpBusinessUser, RegistrationNumber, Arn }

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

  def extractDataFromTokens: PartialFunction[Tokens, A]

  def parseLine(line: String): A = {
    extractDataFromTokens.applyOrElse(
      line.split(delimiter),
      (_: Tokens) => throw LineParsingException(s"Failed to parse due to unexpected format of the line: $line")
    )
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
 *  - records with following structure: 001|XTAL00000100044|ZAGL|Organisation1||||BN12 4XL
 *    where tokens are as follows:
 *      - File Type
 *      - Customer Reference Number
 *      - Customer Tax regime
 *      - Customer Organisation Name
 *      - Customer Title
 *      - Customer Name1
 *      - Customer Name2
 *      - Customer Postal Code
 *  - EOF line: 99|CUSTOMER_DATA|NUMBER_OF_RECORDS
 */
object EtmpBusinessUserParser extends EtmpDataParser[EtmpBusinessUser] {
  def extractDataFromTokens = {
    case Array(_, regNum, _, _, _, _, _, postcode) if regNum.nonEmpty && postcode.nonEmpty =>
      EtmpBusinessUser(RegistrationNumber(regNum), postcode)
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
object EtmpAgentParser extends EtmpDataParser[EtmpAgent] {
  def extractDataFromTokens = {
    case Array(_, arn, _*) if arn.nonEmpty => EtmpAgent(Arn(arn))
  }

  def isRecord(line: String): Boolean = line.startsWith("002|")
}
