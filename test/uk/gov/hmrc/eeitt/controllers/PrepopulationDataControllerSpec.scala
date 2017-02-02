package uk.gov.hmrc.eeitt.controllers

import org.scalatest.concurrent.ScalaFutures
import play.api.Play
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.eeitt.ApplicationComponents
import uk.gov.hmrc.play.test.UnitSpec

class PrepopulationDataControllerSpec extends UnitSpec with ApplicationComponents with ScalaFutures {

  implicit val m = fakeApplication.materializer

  object PrepopulationDataController extends PrepopulationDataControllerHelper {}

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
    }
  }

  "GET /prepopulation/:formId/:cacheId" should {
    "return 404 again for unknown ids" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("iii", "i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "GET /prepopulation/:formId/:cacheId" should {
    "return 200 for known ids" in {
      val fakeRequest = FakeRequest()
      val action = PrepopulationDataController.get("ii", "i-and-i")
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe "{}"
    }
  }

}
