package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.MongoRegistrationRepository
import uk.gov.hmrc.eeitt.services.RegistrationService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }
import uk.gov.hmrc.eeitt.model.VerificationResponse
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  object TestRegistrationService extends RegistrationService {
    val registrationRepo = mock[MongoRegistrationRepository]
    registrationRepo.findRegistrations("1").returns(Future.successful(List(Registration("1", false, "12LT001", "", List("LT", "LL")))))
    registrationRepo.findRegistrations("2").returns(Future.successful(List(Registration("2", false, "12LT002", "", List("LT", "LL", "XT")))))
    registrationRepo.findRegistrations("3").returns(Future.successful(List()))
    registrationRepo.findRegistrations("4").returns(Future.successful(List(
      Registration("4", false, "12LT004", "", List("LT", "LL")),
      Registration("4", false, "12LT005", "", List("LT", "XT"))
    )))
    registrationRepo.findRegistrations("5").returns(Future.successful(List(Registration("5", true, "", "KARN001", List()))))
  }

  //  object TestEnrolmentStoreService extends EnrolmentVerificationService {
  //    val enrolmentRepo = mock[MongoEnrolmentRepository]
  //    enrolmentRepo.lookupEnrolment("12LT001").returns(Future.successful(List(Enrolment("1", "12LT001", true, "SE39EP", ""))))
  //    enrolmentRepo.lookupEnrolment("12LT002").returns(Future.successful(List(Enrolment("1", "12LT002", true, "SE39EP", "agent"))))
  //    enrolmentRepo.getEnrolmentsWithArn("agentx").returns(Future.successful(List()))
  //    enrolmentRepo.lookupEnrolment("12LT32").returns(Future.successful(List()))
  //    enrolmentRepo.lookupEnrolment("12LT33").returns(Future.successful(List(Enrolment("1", "12LT002", true, "SE39EP", "agent"), Enrolment("2", "12LT002", true, "SE39EP", "agent"))))
  //  }

  object TestRegistrationController extends RegistrationController {
    val registrationService = TestRegistrationService
  }

  "GET /group-identifier/:gid/regimes/:regimeid/verification" should {
    "return 200 and is allowed for successful registration lookup where regime is authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }
    "return 200 and is not allowed for successful registration lookup where regime is not authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "ZZ")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is not allowed for an unsuccessful registration lookup" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.verification("3", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is not allowedfor a lookup which returned multiple registration instances" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.verification("4", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is allowed for successful registration lookup of agent" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("5", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }
  }

  "POST /eeitt-auth/register" should {

    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterRequest("1", "LT", "12LT009", "SE39EPX")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }
  }

  "POST /eeitt-auth/register-agent" should {
    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterAgentRequest("1", "KARN001")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }

  }
}