package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.model.{ VerificationResponse, _ }
import uk.gov.hmrc.eeitt.repositories.{ MongoEtmpAgentRepository, MongoEtmpBusinessUsersRepository, MongoRegistrationRepository }
import uk.gov.hmrc.eeitt.services.RegistrationService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito with EtmpFixtures {

  object TestRegistrationService extends RegistrationService {
    val regRepository = mock[MongoRegistrationRepository]
    regRepository.findRegistrations("1").returns(Future.successful(List(Registration("1", false, "12LT001", "", List("LT", "LL")))))
    regRepository.findRegistrations("2").returns(Future.successful(List(Registration("2", false, "12LT002", "", List("LT", "LL", "XT")))))
    regRepository.findRegistrations("3").returns(Future.successful(List()))
    regRepository.findRegistrations("4").returns(Future.successful(List(
      Registration("4", false, "12LT004", "", List("LT", "LL")),
      Registration("4", false, "12LT005", "", List("LT", "XT"))
    )))
    regRepository.findRegistrations("5").returns(Future.successful(List(Registration("5", true, "", "KARN001", List()))))
    val userRepository = mock[MongoEtmpBusinessUsersRepository]
    userRepository.findByRegistrationNumber("12LT009").returns(Future.successful(List()))
    val agentRepository = mock[MongoEtmpAgentRepository]
    agentRepository.findByArn("KARN002").returns(Future.successful(List()))
  }

  object TestRegistrationController extends RegistrationController {
    val registrationService = TestRegistrationService
  }

  "GET /group-identifier/:gid/regimes/:regimeid/verification" should {
    "return 200 and is allowed for successful registration lookup where regime is authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "LT", Individual)(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }
    "return 200 and is not allowed for successful registration lookup where regime is not authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "ZZ", Individual)(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is not allowed for an unsuccessful registration lookup" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.verification("3", "LT", Individual)(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is not allowed for a lookup which returned multiple registration instances" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestRegistrationController.verification("4", "LT", Individual)(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }
    "return 200 and is allowed for successful registration lookup of agent" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("5", "LT", Individual)(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }
  }

  "POST /eeitt-auth/register" should {

    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterRequest("1", "12LT009", Some("SE39EPX"))))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }
  }

  "POST /eeitt-auth/register-agent" should {
    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register-agent").withBody(toJson(RegisterAgentRequest("1", "KARN002", Some("SE39EPX"))))
      val result = TestRegistrationController.registerAgent()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }

  }
}
