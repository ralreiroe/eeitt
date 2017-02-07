package uk.gov.hmrc.eeitt.controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.eeitt.{ApplicationComponentsOnePerSuite, MicroserviceShortLivedCache}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class PrepopulationDataControllerSpec extends UnitSpec with ApplicationComponentsOnePerSuite with ScalaFutures {

  implicit val m = fakeApplication.materializer

  object PrepopulationDataController extends PrepopulationDataControllerHelper {}

  object PrepopulationDataHelper extends BaseController {

    implicit val hc = HeaderCarrier()


    def fetchAndGetEntry(cacheId: String, key: String): Future[Option[JsValue]] =
      MicroserviceShortLivedCache.fetchAndGetEntry[JsValue](cacheId, key)

    def cache(cacheId: String, formId: String, body: JsValue): Future[CacheMap] =
      MicroserviceShortLivedCache.cache[JsValue](cacheId, formId, body)

    def remove(cacheId: String): Future[HttpResponse] =
      MicroserviceShortLivedCache.remove(cacheId)(hc)

  }

  "GET /prepopulation/:cacheId/:formId" should {
    "return 404 for unknown ids" in {
      await(PrepopulationDataHelper.remove("i-and-and"))
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("i-and-i", "iii")
      val result = action(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "GET /prepopulation/:cacheId/:formId" should {
    "return 200 for with correct data for existing cached data" in {
      val fakeRequest = FakeRequest()
      await(PrepopulationDataHelper.cache("i-and-i", "ii", Json.toJson("""{"i":"and-"}""")))
      val action = PrepopulationDataController.get("i-and-i", "ii")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      val await1: Result = await(result)
      Logger.debug(s"Result $await1")
      val of1: JsValue = jsonBodyOf(await1)
      Logger.debug(s"Result $of1")

      of1 shouldBe Json.toJson("""{"i":"and-"}""")
    }
  }

  "PUT /prepopulation/:cacheId/:formId/:jsonData" should {
    "return 200 and the data now cached" in {
      val fakeRequest = FakeRequest().withBody(Json.parse("""{"i":"and-"}"""))
      await(PrepopulationDataHelper.remove("i-and-i"))
      val action = PrepopulationDataController.put("i-and-i", "ii")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      await(PrepopulationDataHelper.fetchAndGetEntry("i-and-i", "ii")) shouldBe
        Some(Json.parse("""{"i":"and-"}"""))
    }
  }

  "PUT /prepopulation/:cacheId/:formId/:jsonData" should {
    "return 200 to put another formId to the same cacheId, both formIds should have correct data" in {
      val fakeRequest = FakeRequest().withBody(Json.parse("""{"i":"and-and"}"""))
      await(PrepopulationDataHelper.remove("i-and-and"))
      await(PrepopulationDataHelper.cache("i-and-and", "i", Json.toJson("""{"i":"and-"}""")))
      val action = PrepopulationDataController.put("i-and-and", "ii")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      await(PrepopulationDataHelper.fetchAndGetEntry("i-and-and", "i")) shouldBe
        Some(Json.toJson("""{"i":"and-"}"""))
      await(PrepopulationDataHelper.fetchAndGetEntry("i-and-and", "ii")) shouldBe
        Some(Json.parse("""{"i":"and-and"}"""))
    }
  }

  "DELETE /prepopulation/:cacheId" should {
    "return 200 and the data should no longer be cached" in {
      val fakeRequest = FakeRequest()
      await(PrepopulationDataHelper.cache("i-and-i", "ii", Json.toJson("""{"i":"and-"}""")))
      val action = PrepopulationDataController.delete("i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.NO_CONTENT
      await(PrepopulationDataHelper.fetchAndGetEntry("i-and-i", "ii")) shouldBe None
    }
  }

}
