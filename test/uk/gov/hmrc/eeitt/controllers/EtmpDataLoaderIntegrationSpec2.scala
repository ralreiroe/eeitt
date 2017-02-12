package uk.gov.hmrc.eeitt.controllers

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import reactivemongo.api.{DefaultDB, MongoConnection}
import uk.gov.hmrc.eeitt.repositories.{MongoEtmpAgentRepository, MongoEtmpBusinessUsersRepository}
import uk.gov.hmrc.eeitt.services.EtmpDataParser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class EtmpDataLoaderIntegrationSpec2 extends UnitSpec with Matchers with ScalaFutures with MongoSpecSupport
{
  implicit val hc = new HeaderCarrier()

  "blah" should {
    "parse Agent payload" in {
      val businessUsersData =
        """|001|XTAL00000100044888|||||||||BN124XL|GB""".stripMargin

//      implicit lazy val db: () => DefaultDB = () => DefaultDB("", null)
      val burepo = new MongoEtmpBusinessUsersRepository

      val res: Future[EtmpDataLoaderResult] = EtmpDataLoader.load(businessUsersData)(EtmpDataParser.parseFileWithBusinessUsers, burepo.replaceAll, burepo.report)

      await(res, Duration.Inf)

      val res2: EtmpDataLoaderResult = res.futureValue

      println(res2)

    }
  }

}
