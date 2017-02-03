package uk.gov.hmrc.eeitt.controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Play
import play.api.http.Status
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.eeitt.model.PrepopulationJsonData
import uk.gov.hmrc.eeitt.{ApplicationComponentsOnePerSuite, MicroserviceShortLivedCache}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class PrepopulationDataControllerSpec extends UnitSpec with ApplicationComponentsOnePerSuite with ScalaFutures {

  implicit val m = fakeApplication.materializer

  object PrepopulationDataController extends PrepopulationDataControllerHelper {}

  object PrepopulationDataHelper extends BaseController {
    val hc = HeaderCarrier()
    def fetchAndGetEntry[T](cacheId: String,
                            key: String, rds: Reads[T]): Future[Option[T]] =
      MicroserviceShortLivedCache.fetchAndGetEntry[T](cacheId, key)(hc, rds)

    def cache[A](cacheId: String,
                 formId: String, body: A)(implicit hc: HeaderCarrier,
                                          wts: Writes[A]): Future[CacheMap] =
      MicroserviceShortLivedCache.cache[A](cacheId, formId, body)(hc, wts)

    }

  "GET /prepopulation/:formId/:cacheId" should {
    "return 404 for unknown ids" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("iii", "i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "PUT /prepopulation/:formId/:cacheId/:jsonData" should {
    "return 200" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.put("ii", "i-and-i", "{}")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      val p = PrepopulationDataHelper.fetchAndGetEntry[PrepopulationJsonData]("i-and-i", "ii", PrepopulationJsonData.formatReads)
      await(p) shouldBe Some(PrepopulationJsonData("{}"))
    }
  }

  "GET /prepopulation/:formId/:cacheId" should {
    "return 200 for with correct data for what was put" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("ii", "i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe "{}"
    }
  }

  "PUT /prepopulation/:formId/:cacheId/:jsonData" should {
    "return 200 to put another formId to the same cacheId" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.put("ii", "i-and-and", """{"i":"and"}""")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET /prepopulation/:formId/:cacheId" should {
    "return 200 for with correct data for what was put to the second formId" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("ii", "i-and-and")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe """{"i":"and"}"""
    }
  }
  "GET /prepopulation/:formId/:cacheId" should {
    "return 200 for with correct data for what was put to the first formId" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("ii", "i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe "{}"
    }
  }

}
