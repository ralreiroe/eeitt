package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.model.{ Registration, RegistrationLookupResponse }
import uk.gov.hmrc.eeitt.repositories.MongoRegistrationRepository
import uk.gov.hmrc.eeitt.services.RegistrationLookupService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ RESPONSE_NOT_FOUND, MULTIPLE_FOUND }

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  private val registration1 = Registration("1", List("LT", "LL"), "12LT001", "SE39EP")
  private val registration2 = Registration("2", List("LT", "LL", "XT"), "12LT002", "SE39EX")

  object TestRegistrationLookupService extends RegistrationLookupService {
    val registrationRepo = mock[MongoRegistrationRepository]
    registrationRepo.lookupRegistration("1").returns(Future.successful(List(registration1)))
    registrationRepo.lookupRegistration("2").returns(Future.successful(List(registration2)))
    registrationRepo.lookupRegistration("3").returns(Future.successful(List()))
    registrationRepo.lookupRegistration("4").returns(Future.successful(List(
      Registration("4", List("LT", "LL"), "12LT004", "SE38ZZ"),
      Registration("4", List("LT", "XT"), "12LT005", "SE39ZZ")
    )))
  }

  object TestRegistrationController extends RegistrationController {
    val registrationLookupService = TestRegistrationLookupService
  }

  "GET /regimes/1" should {
    "return 200 and correct response for successful registration lookup" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.regimes("1")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RegistrationLookupResponse(None, Some(registration1)))
    }
  }

  "GET /regimes/3" should {
    "return 200 and error response for an unsuccessful registration lookup" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.regimes("3")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RESPONSE_NOT_FOUND)
    }
  }

  "GET /regimes/4" should {
    "return 200 and error response for a lookup which returned multiple registration instances" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.regimes("4")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(MULTIPLE_FOUND)
    }
  }

}
