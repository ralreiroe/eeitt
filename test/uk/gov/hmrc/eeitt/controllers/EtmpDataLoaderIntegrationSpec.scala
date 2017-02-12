package uk.gov.hmrc.eeitt.controllers

import akka.util.ByteString
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc.{ Action, Result }
import play.api.test.FakeRequest
import uk.gov.hmrc.eeitt.model.Postcode
import uk.gov.hmrc.eeitt.repositories.{ MongoEtmpAgentRepository, MongoEtmpBusinessUsersRepository }
import uk.gov.hmrc.eeitt.services.{ AuditService, EtmpDataParser }
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.http.HeaderCarrier

class EtmpDataLoaderIntegrationSpec extends FlatSpec with Matchers with MongoSpecSupport with ScalaFutures {
  implicit val hc = new HeaderCarrier()

  it should "parse Agent payload" in {
    val agentData =
      """002|WARN0000367|||||||||EH6 4RU|GB|XLAP00000100469888||||||||||""".stripMargin

    val burepo = new MongoEtmpBusinessUsersRepository
    val agrepo = new MongoEtmpAgentRepository

    val res = EtmpDataLoader.load(agentData)(EtmpDataParser.parseFileWithAgents, agrepo.replaceAll)

  }

}
