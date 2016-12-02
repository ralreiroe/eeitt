package uk.gov.hmrc.eeitt.controllers

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import reactivemongo.api.commands.{ MultiBulkWriteResult, Upserted, WriteError }

import scala.concurrent.Future
import uk.gov.hmrc.eeitt.model.{ EtmpBusinessUser, RegisterAgentRequest, RegisterBusinessUserRequest }
import uk.gov.hmrc.eeitt.services.{ AuditService, EtmpDataParser }
import uk.gov.hmrc.play.http.HeaderCarrier

class EtmpDataLoaderSpec extends FlatSpec with Matchers with ScalaFutures {
  implicit val hc = new HeaderCarrier()

  val auditServiceStub = new AuditService {
    override def sendRegisteredBusinessUserEvent(path: String, request: RegisterBusinessUserRequest, tags: Map[String, String])(implicit hc: HeaderCarrier): Unit = {}

    override def sendRegisteredAgentEvent(path: String, request: RegisterAgentRequest, tags: Map[String, String])(implicit hc: HeaderCarrier): Unit = {}

    override def sendDataLoadEvent(path: String, tags: Map[String, String])(implicit hc: HeaderCarrier): Unit = {}
  }

  val etmpDataLoader = new EtmpDataLoader(auditServiceStub)

  "EtmpDataLoader" should "fail to parse empty input" in {
    val res = etmpDataLoader.load("")(EtmpDataParser.parseFileWithBusinessUsers, etmpDataLoader.dryRun)
    res.futureValue should be(ParsingFailure(
      Json.obj(
        "message" -> "No single line was parsed from request body.",
        "body" -> ""
      )
    ))
  }

  it should "fail to parse invalid input" in {
    val res = etmpDataLoader.load("invalidInput")(EtmpDataParser.parseFileWithBusinessUsers, etmpDataLoader.dryRun)
    res.futureValue should be(ParsingFailure(
      Json.obj(
        "message" -> "No single line was parsed from request body.",
        "body" -> "invalidInput"
      )
    ))
  }

  it should "parse Agent payload" in {
    val agentData =
      """|00|AGENT_DATA|ETMP|MDTP|20161103|141316
         |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
         |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XRIP00000100053|ZIPT|Insurance premium tax (IPT)|1|Sole Proprietor||Mr|Name11|Name21|1234567891|IT
         |002|KARN0000086|ARN|Agent Reference Number(ARN)|7|Company|Agent1||||BN12 4XL|GB|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
         |002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
         |002|KARN0000087|ARN|Agent Reference Number(ARN)|7|Company|Agent2||||BN12 4XL|GB|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23||RO
         |002|KARN0000088|ARN|Agent Reference Number(ARN)|1|Sole proprietor||Mr|AGName1|AGName2||RO|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO
         |99|AGENT_DATA|000000006""".stripMargin

    val res = etmpDataLoader.load(agentData)(EtmpDataParser.parseFileWithAgents, etmpDataLoader.dryRun)
    res.futureValue should be(LoadOk(Json.obj("message" -> "3 unique objects imported successfully")))
  }

  it should "parse Business Users payload" in {

    val businessUsersData =
      """|00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
         |001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
         |001|XMAP00000100051|ZAPD|Air Passenger Duty (APD)|7|Limited Company|Organisation2||||BN12 4XL|GB
         |001|XRIP00000100053|ZIPT|Insurance premium tax (IPT)|1|Sole Proprietor||Mr|Name11|Name21||IT
         |001|XSBD00440000020|ZBD|Bingo Duty (BD)|7|Limited Company|Organisation3||||BN12 4XL|GB
         |001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
         |001|XALF00000100019|ZLFT|Landfill Tax Registration Number|1|Sole Proprietor||Mr|Name13|Name23||RO
         |001|XDLD00000100001|ZLD|Lottery Duty (LD)|1|Sole Proprietor||Mr|Name12|Name22||RO
         |99|CUSTOMER_DATA|000000007""".stripMargin

    val res = etmpDataLoader.load(businessUsersData)(EtmpDataParser.parseFileWithBusinessUsers, etmpDataLoader.dryRun)
    res.futureValue should be(LoadOk(Json.obj("message" -> "7 unique objects imported successfully")))
  }

  it should "fail if input payload is corrupted" in {

    val corruptedData =
      """|00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
         |001|XTAL00000100
         |99|CUSTOMER_DATA|000000007""".stripMargin

    val res = etmpDataLoader.load(corruptedData)(EtmpDataParser.parseFileWithBusinessUsers, etmpDataLoader.dryRun)
    res.futureValue should be(ParsingFailure(Json.obj("message" -> "Failed to parse due to unexpected format of line: 001|XTAL00000100")))
  }

  it should "fail if insert to mongo don't insert enough elements" in {

    val businessUsersData =
      """|00|CUSTOMER_DATA|ETMP|MDTP|20161103|141116
         |001|XTAL00000100044|ZAGL|Aggregate Levy (AGL)|7|Limited Company|Organisation1||||BN12 4XL|GB
         |99|CUSTOMER_DATA|000000007""".stripMargin

    val updateZeroRecords: Seq[EtmpBusinessUser] => Future[MultiBulkWriteResult] = _ =>
      Future.successful(MultiBulkWriteResult(true, 0, 0, Seq.empty[Upserted], Seq.empty[WriteError], None, None, None, 0))

    val res = etmpDataLoader.load(businessUsersData)(EtmpDataParser.parseFileWithBusinessUsers, updateZeroRecords)
    res.futureValue should be(ServerFailure(Json.obj(
      "message" -> "Failed to replace existing records with 1 new ones",
      "details" -> "MultiBulkWriteResult(true,0,0,List(),List(),None,None,None,0)"
    )))
  }
}
