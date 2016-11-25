package uk.gov.hmrc.eeitt.controllers

import org.scalatest.{ AppendedClues, Inside }
import play.api.http.Status
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.services.PrepopulationData
import uk.gov.hmrc.eeitt.{ EtmpFixtures, RegistrationFixtures, TypeclassFixtures }
import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.model.{ VerificationResponse, _ }
import uk.gov.hmrc.eeitt.services.{ FindRegistration, RegistrationService }
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with AppendedClues with Inside with EtmpFixtures with RegistrationFixtures with TypeclassFixtures {

  object TestRegistrationController extends RegistrationController {}

  "GET /group-identifier/:gid/regimes/:regimeid/verification" should {
    "return 200 and is allowed for successful registration lookup where regime is authorised" in {
      val fakeRequest = FakeRequest()
      implicit val a = findRegistration(List(testRegistrationBusinessUser())) { req: (GroupId, RegimeId) => }
      val action = TestRegistrationController.verify((GroupId("1"), RegimeId("ZZ")))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }

    "return 200 and is not allowed for successful registration lookup where regime is not authorised" in {
      val fakeRequest = FakeRequest()
      implicit val a = findRegistration(List.empty[RegistrationBusinessUser]) { req: (GroupId, RegimeId) => }
      val action = TestRegistrationController.verify((GroupId("1"), RegimeId("ZZ")))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }

    "return 200 and is not allowed for a lookup which returned multiple registration instances" in {
      val fakeRequest = FakeRequest()
      implicit val a = findRegistration(List(testRegistrationBusinessUser(), testRegistrationBusinessUser())) { req: (GroupId, RegimeId) => }

      val action = TestRegistrationController.verify((GroupId("1"), RegimeId("ZZ")))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(false))
    }

    "return 200 and is allowed for successful registration lookup of agent" in {
      val fakeRequest = FakeRequest()
      implicit val a = findRegistration(List(testRegistrationAgent())) { req: GroupId => }
      val action = TestRegistrationController.verify(GroupId("1"))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(VerificationResponse(true))
    }
  }

  "POST /eeitt-auth/register" should {

    "return 200 and error if submitted known facts are different than stored known facts about business user" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterBusinessUserRequest(GroupId("1"), RegistrationNumber("1234567890ABCDE"), Some(Postcode("SE39EPX")))))

      implicit val a = addRegistration(Right(())) { req: RegisterBusinessUserRequest => /* is not called */ }
      implicit val b = findRegistration(List.empty[RegisterBusinessUserRequest]) { req: RegisterBusinessUserRequest => /* is not called */ }

      implicit val c = findUser(List.empty[EtmpBusinessUser]) { req: RegisterBusinessUserRequest =>
        inside(req) {
          case RegisterBusinessUserRequest(groupId, registrationNumber, postcode) =>
            groupId.value should be("1")
            registrationNumber.value should be("1234567890ABCDE")
            convertOptionToValuable(postcode).value.value should be("SE39EPX")
        }
      }

      val action = TestRegistrationController.register[RegisterBusinessUserRequest, EtmpBusinessUser]
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS_BUSINESS_USERS)
    }

    "return 200 and error if submitted known facts are different than stored known facts about agent" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegisterAgentRequest(GroupId("1"), Arn("12LT009"), Some(Postcode("SE39EPX")))))

      implicit val a = addRegistration(Right(())) { req: RegisterAgentRequest => /* is not called */ }
      implicit val b = findRegistration(List.empty[RegisterAgentRequest]) { req: RegisterAgentRequest => /* is not called */ }

      implicit val c = findUser(List.empty[EtmpAgent]) { req: RegisterAgentRequest =>
        inside(req) {
          case RegisterAgentRequest(groupId, arn, postcode) =>
            groupId.value should be("1")
            arn.value should be("12LT009")
            convertOptionToValuable(postcode).value.value should be("SE39EPX")
        }
      }

      val action = TestRegistrationController.register[RegisterAgentRequest, EtmpAgent]
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS_AGENTS)
    }

    "return 400 and error if submitted known facts are different than stored known facts about agent" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(Json.obj("invalid-request-json" -> "dummy"))

      implicit val a = addRegistration(Right(())) { req: RegisterAgentRequest => /* is not called */ }
      implicit val b = findRegistration(List.empty[RegisterAgentRequest]) { req: RegisterAgentRequest => /* is not called */ }
      implicit val c = findUser(List.empty[EtmpAgent]) { req: RegisterAgentRequest => /* is not called */ }

      val action = TestRegistrationController.register[RegisterAgentRequest, EtmpAgent]
      val result = action(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
    }
  }

  "Prepopulation" should {
    "return arn for agent" in {
      val fakeRequest = FakeRequest()
      val agent = testRegistrationAgent()
      implicit val a = findRegistration(List(agent)) { req: GroupId => }
      val action = TestRegistrationController.prepopulate[GroupId, RegistrationAgent](GroupId("1"))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe Json.obj("arn" -> agent.arn.value)
    }

    "return 404 where arn for agent is not found in db" in {
      val fakeRequest = FakeRequest()
      implicit val a = findRegistration(List.empty[RegistrationAgent]) { req: GroupId => }
      val action = TestRegistrationController.prepopulate[GroupId, RegistrationAgent](GroupId("1"))
      val result = action(fakeRequest)
      status(result) shouldBe Status.NOT_FOUND
      bodyOf(await(result)) shouldBe ""
    }

    "return first arn of first agent if there are more agents in db" in {
      val fakeRequest = FakeRequest()
      val agent1 = testRegistrationAgent()
      val agent2 = testRegistrationAgent()
      implicit val a = findRegistration(List(agent1, agent2)) { req: GroupId => }
      val action = TestRegistrationController.prepopulate[GroupId, RegistrationAgent](GroupId("1"))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe Json.obj("arn" -> agent1.arn.value)
    }

    "return registrationNumber for business user" in {
      val fakeRequest = FakeRequest()
      val businessUser = testRegistrationBusinessUser()
      implicit val a = findRegistration(List(businessUser)) { req: (GroupId, RegimeId) => }
      val action = TestRegistrationController.prepopulate[(GroupId, RegimeId), RegistrationBusinessUser]((GroupId("1"), RegimeId("ZZ")))
      val result = action(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe Json.obj("registrationNumber" -> businessUser.registrationNumber.value)
    }
  }
}
