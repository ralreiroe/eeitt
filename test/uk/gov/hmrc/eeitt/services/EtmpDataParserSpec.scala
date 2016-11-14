package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.play.test.UnitSpec

class EtmpDataParserSpec extends UnitSpec {

  "Parsing individual lines of a flat file containing ETMP business users" should {
    "succeed if line matches expected format" in {
      val validLine = "001|regNum|taxReg|taxRegDesc|orgType|orgTypeDesc|orgName|title|name1|name2|postcode|countryCode"
      noException should be thrownBy EtmpBusinessUserParser.parseLine(validLine)
    }

    "fail if registration number (aka customer identification number) is missing" in {
      val missingOrEmptyRegNumber = List(
        "001||taxReg|taxRegDesc|orgType|orgTypeDesc|orgName|title|name1|name2|postcode|countryCode",
        "001|     |taxReg|taxRegDesc|orgType|orgTypeDesc|orgName|title|name1|name2|postcode|countryCode"
      )

      missingOrEmptyRegNumber.foreach { line =>
        val exception = intercept[LineParsingException] { EtmpBusinessUserParser.parseLine(line) }
        exception.msg shouldBe s"Missing registrationNumber in line: $line"
      }
    }

    "fail if customer is from the UK but doesn't have a postcode" in {
      val missingOrEmptyPostcode = List(
        "001|regNum|taxReg|taxRegDesc|orgType|orgTypeDesc|orgName|title|name1|name2||GB",
        "001|regNum|taxReg|taxRegDesc|orgType|orgTypeDesc|orgName|title|name1|name2|         |GB"
      )

      missingOrEmptyPostcode.foreach { line =>
        val exception = intercept[LineParsingException] { EtmpBusinessUserParser.parseLine(line) }
        exception.msg shouldBe s"Missing postcode for a UK entity in line: $line"
      }
    }

    "fail if line contains incorrect number of tokens" in {
      val invalidLine = "foo|bar"

      val exception = intercept[LineParsingException] {
        EtmpBusinessUserParser.parseLine(invalidLine)
      }

      exception.msg shouldBe s"Failed to parse due to unexpected format of line: $invalidLine"
    }

    // todo specification gives many additional rules for ETMP data set however we don't care for now

  }

  "Parsing a flat file with ETMP business users data" should {
    "be successful" in {
      val flatFile =
        """
          >00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
          >001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
          >001|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
          >001|XRIP00000100053|ZIPT|Insurance premium tax (IPT)|1|Sole Proprietor||Mr|Name11|Name21||IT
          >001|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
          >001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
          >001|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23||RO
          >001|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO
          >99|CUSTOMER_DATA|000000007
          >
        """.stripMargin('>')

      EtmpBusinessUserParser.parseFile(flatFile).size shouldBe 7
    }
  }

  "Parsing individual lines of a flat file containing ETMP agents" should {
    "succeed if line matches expected format" in {
      val validLine = "fileType|arn|AgentIdType|AgentIdTypeDesc|AgentOrgType|AgentOrgTypeDesc|AgentOrgName|AgentTitle|AgentName1|AgentName2|AgentPostcode|AgentCountryCode|CustRegNum|CustTaxReg|CustTaxRegDesc|CustOrgType|CustOrgTypeDesc|CustOrgName|CustTitle|CustName1|CustName2|CustPostcode|CustCountryCode"
      noException should be thrownBy EtmpAgentRecordParser.parseLine(validLine)
    }

    "fail if Agent Reference Number (arn) is missing" in {
      val missingOrEmptyARN = List(
        "fileType|    |AgentIdType|AgentIdTypeDesc|AgentOrgType|AgentOrgTypeDesc|AgentOrgName|AgentTitle|AgentName1|AgentName2|AgentPostcode|AgentCountryCode|CustRegNum|CustTaxReg|CustTaxRegDesc|CustOrgType|CustOrgTypeDesc|CustOrgName|CustTitle|CustName1|CustName2|CustPostcode|CustCountryCode",
        "fileType||AgentIdType|AgentIdTypeDesc|AgentOrgType|AgentOrgTypeDesc|AgentOrgName|AgentTitle|AgentName1|AgentName2|AgentPostcode|AgentCountryCode|CustRegNum|CustTaxReg|CustTaxRegDesc|CustOrgType|CustOrgTypeDesc|CustOrgName|CustTitle|CustName1|CustName2|CustPostcode|CustCountryCode"
      )

      missingOrEmptyARN.foreach { line =>
        val exception = intercept[LineParsingException] { EtmpAgentRecordParser.parseFile(line) }
        exception.msg shouldBe s"Missing arn in line: $line"
      }
    }
    "fail if line contains incorrect number of tokens" in {
      val invalidLine = "not|enough|tokens"
      a[LineParsingException] should be thrownBy EtmpAgentRecordParser.parseLine(invalidLine)
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

      EtmpAgentRecordParser.parseFile(flatFile).size shouldBe 6
    }
  }

}
