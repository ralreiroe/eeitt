package uk.gov.hmrc.eeitt.controllers

import play.api.http.Status
import play.api.libs.json.Reads
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class PrepopulationControllerUnitSpec extends UnitSpec {

  class ShortLivedCacheStub extends ShortLivedCache {

    override def shortLiveCache = ???
    override implicit val crypto: CompositeSymmetricCrypto = null
  }

  "GET /prepopulation/:cacheId/:formId" should {
    "return 204 (No Content) for unknown ids" in {

      val cacheReturningNoneUponFetch = new ShortLivedCacheStub {
        override def fetchAndGetEntry[T](cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T]): Future[Option[T]] = Future.successful(None)
      }

      val irrelevant = "1"
      val eventualResult: Future[Result] = new PrepopulationDataController(cacheReturningNoneUponFetch).get(irrelevant, irrelevant)(FakeRequest())

      await(eventualResult).header.status shouldBe Status.NO_CONTENT
    }
  }
}
