package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.{ MongoEtmpAgentRepository, MongoEtmpBusinessUsersRepository, MongoRegistrationRepository }
import uk.gov.hmrc.eeitt.services.RegistrationService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }
import uk.gov.hmrc.eeitt.model.VerificationResponse
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  object TestRegistrationService extends RegistrationService {
    val regRepository = mock[MongoRegistrationRepository]
    regRepository.findRegistrations(GroupId("1"), RegimeId("LT")).returns(Future.successful(List(IndividualRegistration(GroupId("1"), RegistrationNumber("12LT001"), RegimeId("LT")))))
    regRepository.findRegistrations(GroupId("2"), RegimeId("LT")).returns(Future.successful(List(IndividualRegistration(GroupId("2"), RegistrationNumber("12LT002"), RegimeId("LT")))))
    regRepository.findRegistrations(GroupId("3"), RegimeId("LT")).returns(Future.successful(List()))
    regRepository.findRegistrations(GroupId("4"), RegimeId("LT")).returns(
      Future.successful(
        List(
          IndividualRegistration(GroupId("4"), RegistrationNumber("12LT004"), RegimeId("LT")),
          IndividualRegistration(GroupId("4"), RegistrationNumber("12LT005"), RegimeId("LT"))
        )
      )
    )
    regRepository.findRegistrations(GroupId("5"), RegimeId("LT")).returns(
      Future.successful(
        List(
          IndividualRegistration(GroupId("5"), RegistrationNumber("KARN001"), RegimeId("LT"))
        )
      )
    )
    val userRepository = mock[MongoEtmpBusinessUsersRepository]
    userRepository.userExists(EtmpBusinessUser(RegistrationNumber("12LT001"), "SE39EP")).returns(Future.successful(true))
    userRepository.userExists(EtmpBusinessUser(RegistrationNumber("12LT009"), "SE39EPX")).returns(Future.successful(false))
    val agentRepository = mock[MongoEtmpAgentRepository]
    agentRepository.agentExists(EtmpAgent(Arn("KARN001"))).returns(Future.successful(true))
    agentRepository.agentExists(EtmpAgent(Arn("KARN002"))).returns(Future.successful(false))
  }

  object TestRegistrationController extends RegistrationController {
    val registrationService = TestRegistrationService
  }

  "GET /group-identifier/:gid/regimes/:regimeid/verification" should {
    /* "return 200 and is allowed for successful registration lookup where regime is authorised" in {
     *   val fakeRequest = FakeRequest()
     *   val result = TestRegistrationController.verification("1", "LT", Individual)(fakeRequest)
     *   status(result) shouldBe Status.OK
     *   jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
     * } */
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
    /* "return 200 and is allowed for successful registration lookup of agent" in {
     *   val fakeRequest = FakeRequest()
     *   val result = TestRegistrationController.verification("5", "LT", Individual)(fakeRequest)
     *   status(result) shouldBe Status.OK
     *   jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
     * } */
  }

  "POST /eeitt-auth/register" should {

    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterRequest(GroupId("1"), RegistrationNumber("12LT009"), "SE39EPX")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }
  }

  "POST /eeitt-auth/register-agent" should {
    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register/agent").withBody(toJson(RegisterAgentRequest(GroupId("1"), Arn("KARN002"))))
      val result = TestRegistrationController.registerAgent()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }
  }
}
