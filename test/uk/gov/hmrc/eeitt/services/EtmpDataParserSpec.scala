package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.play.test.UnitSpec

class EtmpDataParserSpec extends UnitSpec {

  "Parsing individual lines of a flat file containing ETMP business users" should {
    "succeed if line matches expected format" in {
      val validLines = List(
        "|regNumber||||||postcode", // regNumber and postcode provided
        "a|regNumber|b|c|d|e|f|postcode" // other things provided as well
      )
      validLines.foreach { l =>
        noException should be thrownBy EtmpBusinessUserParser.parseLine(l)
      }
    }
    "fail otherwise" in {
      val invalidLines = List(
        "|||||||postcode", // missing regNumber
        "|regNumber||||||", // missing postcode
        "|||" // incorrect number of tokens
      )
      invalidLines.foreach { l =>
        a[LineParsingException] should be thrownBy EtmpBusinessUserParser.parseLine(l)
      }
    }
  }

  "Parsing a flat file with ETMP business users data" should {
    "be successful" in {
      val flatFile =
        """
          >00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
          >001|XTAL00000100044|ZAGL|Organisation1||||BN12 4XL
          >001|XMAP00000100051|ZAPD|Organisation2||||BN12 4XL
          >001|XSBD00440000020|ZBD|Organisation3||||BN12 4XL
          >001|XXGD00450000018|ZGD|Organisation4||||BN12 4XL
          >001|XRIP00000100053|ZIPT||Mr|Name11|Name21|BN12 4XL
          >001|XDLD00000100001|ZLD||Mr|Name12|Name22|BN12 4XL
          >001|XALF00000100019|ZLFT||Mr|Name13|Name23|BN12 4XL
          >99|CUSTOMER_DATA|000000007
          >
        """.stripMargin('>')

      EtmpBusinessUserParser.parseFile(flatFile).size shouldBe 7
    }
  }

  "Parsing individual lines of a flat file containing ETMP agents" should {
    "succeed if line matches expected format" in {
      val validLines = List(
        "|arn", // arn provided as 2nd token
        "a|arn|b|c|d|e|f|g|h|i|j|k|l||||||||" // other things provided as well
      )
      validLines.foreach { l =>
        noException should be thrownBy EtmpAgentParser.parseLine(l)
      }
    }
    "fail otherwise" in {
      val invalidLines = List(
        "|||||||" // missing arn
      )
      invalidLines.foreach { l =>
        a[LineParsingException] should be thrownBy EtmpAgentParser.parseLine(l)
      }
    }
  }

  "Parsing a flat file with ETMP agent data" should {
    "be successful" in {
      val flatFile =
        """
          >00|AGENT_DATA|ETMP|MDTP|20161103|141316
          >002|KARN0000086|ARN|Agent1||||BN12 4XL|XMAP00000100051|ZAPD|Organisation1|||
          >002|KARN0000086|ARN|Agent1||||BN12 4XL|XRIP00000100053|ZIPT||Mr|Name11|Name21
          >002|KARN0000086|ARN|Agent1||||BN12 4XL|XSBD00440000020|ZBD|Organisation3|||
          >002|KARN0000087|ARN|Agent2||||BN12 4XL|XTAL00000100044|ZAGL|Organisation1|||
          >002|KARN0000087|ARN|Agent2||||BN12 4XL|XRIP00000100053|ZIPT||Mr|Name11|Name21
          >002|KARN0000088|ARN|Agent3||Mr|Name1|Name2|BN12 4XL|XDLD00000100001|ZLD|||Mr|Name12|Name22
          >99|AGENT_DATA|000000006
          >
        """.stripMargin('>')

      EtmpAgentParser.parseFile(flatFile).size shouldBe 6
    }
  }

}
