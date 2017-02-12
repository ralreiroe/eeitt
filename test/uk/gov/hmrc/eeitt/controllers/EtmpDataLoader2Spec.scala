package uk.gov.hmrc.eeitt.controllers

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import reactivemongo.api.commands.{ MultiBulkWriteResult, Upserted, WriteError }
import uk.gov.hmrc.eeitt.model.EtmpBusinessUser
import uk.gov.hmrc.eeitt.services.EtmpDataParser
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class EtmpDataLoader2Spec extends FlatSpec with Matchers with ScalaFutures {
  implicit val hc = new HeaderCarrier()

  it should "parse Agent payload" in {
    val agentData =
      """002|WARN0000367|||||||||EH6 4RU|GB|XLAP00000100469888||||||||||""".stripMargin

    val res = EtmpDataLoader.load(agentData)(EtmpDataParser.parseFileWithAgents, EtmpDataLoader.dryRun)
    res.futureValue should be(LoadOk(Json.obj("message" -> "1 unique objects imported successfully"), 1))
  }

}
