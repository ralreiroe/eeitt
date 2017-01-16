package uk.gov.hmrc.eeitt.utils

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.{ Arn, EtmpAgent, EtmpBusinessUser, RegistrationNumber }
import uk.gov.hmrc.eeitt.repositories.MongoEtmpAgentRepository
import uk.gov.hmrc.eeitt.services.EtmpDataParser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class DiffSpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with EtmpFixtures {

  "diff" should {
    "contain list of added, removed and changed AGENT records" in {

      val agentDataold: String =
        """002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
          |002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23||RO
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XRIP00000100053|ZAPD|Air Passenger Duty (APD)|1|Sole Proprietor||Mr|Name11|Name21|1234567891|IT
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
          |002|KARN0000088|ARN|Agent Reference Number(ARN)|1|Sole proprietor||Mr|AGName1|AGName2||RO|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO""".stripMargin

      val agentDatanew =
        """002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
          |NEXT LINE CHANGED (CLIENT POSTCODE AND COUNTRYCODE):
          |002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23|BN124XL|GB
          |NEXT LINE NEW:
          |002|KARN0000089|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XALF00000100222|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name14|Name24||RO
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XRIP00000100053|ZAPD|Air Passenger Duty (APD)|1|Sole Proprietor||Mr|Name11|Name21|1234567891|IT
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XRIP00000100054|ZAPD|Air Passenger Duty (APD)|1|Sole Proprietor||Mr|Name12|Name22|1234567892|IT
          |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
          |REMOVED:|KARN0000088|ARN|Agent Reference Number(ARN)|1|Sole proprietor||Mr|AGName1|AGName2||RO|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO""".stripMargin

      val diff = Differ.diff[EtmpAgent, Arn](
        EtmpDataParser.parseFileWithAgents(agentDataold),
        EtmpDataParser.parseFileWithAgents(agentDatanew), _.arn
      )

      diff shouldBe
        Diff(
          List(Arn("KARN0000089")),
          List(Arn("KARN0000088")),
          List(Arn("KARN0000086"), Arn("KARN0000087"))
        )

    }
  }

  "diff" should {
    "contain list of added, removed and changed BUSINESS USER records" in {

      val userfile1 =
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

      val userfile2 =
        """
          >00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
          >001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
          >REMOVED:|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
          >NEXT LINE ADDED:
          >001|XMAP00000100066|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
          >001|XRIP00000100053|ZIPT|Insurance premium tax (IPT)|1|Sole Proprietor||Mr|Name11|Name21||IT
          >001|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
          >NEXT LINE CHANGED (POSTCODE):
          >001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 5XL|GB
          >001|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23||RO
          >001|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO
          >99|CUSTOMER_DATA|000000007
          >
        """.stripMargin('>')

      val businessUsers1 = EtmpDataParser.parseFileWithBusinessUsers(userfile1)
      val businessUsers2 = EtmpDataParser.parseFileWithBusinessUsers(userfile2)
      Differ.diff[EtmpBusinessUser, String](businessUsers1, businessUsers2, _.registrationNumber.value) shouldBe
        Diff(
          added = List("XMAP00000100066"),
          removed = List("XMAP00000100051"),
          changed = List("XTAL00000100044")
        )

    }

  }

  val repo = new MongoEtmpAgentRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

}
