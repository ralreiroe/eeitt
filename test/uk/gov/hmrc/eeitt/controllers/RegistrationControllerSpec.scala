package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.model.{ RegistrationRequest, _ }
import uk.gov.hmrc.eeitt.repositories.{ MongoEnrolmentRepository, MongoRegistrationRepository }
import uk.gov.hmrc.eeitt.services.{ EnrolmentVerificationService, RegistrationService }
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ MULTIPLE_FOUND, RESPONSE_NOT_FOUND }
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.Future

class RegistrationControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  private val registration1 = Registration("1", List("LT", "LL"), "12LT001", "SE39EP")
  private val registration2 = Registration("2", List("LT", "LL", "XT"), "12LT002", "SE39EX")

  object TestRegistrationService extends RegistrationService {
    val registrationRepo = mock[MongoRegistrationRepository]
    registrationRepo.findRegistrations("1").returns(Future.successful(List(registration1)))
    registrationRepo.findRegistrations("2").returns(Future.successful(List(registration2)))
    registrationRepo.findRegistrations("3").returns(Future.successful(List()))
    registrationRepo.findRegistrations("4").returns(Future.successful(List(
      Registration("4", List("LT", "LL"), "12LT004", "SE38ZZ"),
      Registration("4", List("LT", "XT"), "12LT005", "SE39ZZ")
    )))
  }

  object TestEnrolmentStoreService extends EnrolmentVerificationService {
    val enrolmentRepo = mock[MongoEnrolmentRepository]
    enrolmentRepo.lookupEnrolment("12LT001").returns(Future.successful(List(Enrolment("1", "12LT001", true, "SE39EP", ""))))
    enrolmentRepo.lookupEnrolment("12LT002").returns(Future.successful(List(Enrolment("1", "12LT002", true, "SE39EP", "agent"))))
    enrolmentRepo.getEnrolmentsWithArn("agentx").returns(Future.successful(List()))
    enrolmentRepo.lookupEnrolment("12LT32").returns(Future.successful(List()))
    enrolmentRepo.lookupEnrolment("12LT33").returns(Future.successful(List(Enrolment("1", "12LT002", true, "SE39EP", "agent"), Enrolment("2", "12LT002", true, "SE39EP", "agent"))))
  }

  object TestRegistrationController extends RegistrationController {
    val registrationService = TestRegistrationService
    val enrolmentVerificationService = TestEnrolmentStoreService
  }

  "GET /group-identifier/:gid/regimes/:regimeid/verification" should {
    "return 200 and correct response for successful registration lookup where regime is authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RegistrationLookupResponse(true))
    }
  }

  "GET /group-identifier/1/regimes/ZZ/verification" should {
    "return 200 and correct response for successful registration lookup where regime is not authorised" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("1", "ZZ")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RegistrationLookupResponse(false))
    }
  }

  "GET /group-identifier/99/regimes/LT/verification" should {
    "return 200 and correct response for unsuccessful registration lookup" in {
      val fakeRequest = FakeRequest()
      val result = TestRegistrationController.verification("99", "LT")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RegistrationLookupResponse(true))
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

  "POST /register" should {
    //    case class RegistrationRequest(groupId: String, regimeId: String, registrationNumber: String, postcode: String,
    //                                   formTypeRef: String, livesInTheUk: Boolean, isAgent: Boolean, arn: String)

    // case class EnrolmentVerificationRequest(formTypeRef: String, registrationNumber: String, livesInTheUk: Boolean, postcode: String, isAgent: Boolean, arn: String)

    "return 200 and error if submitted known facts are different than stored known facts" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/register").withBody(toJson(RegistrationRequest("1", "LT", "12LT009", "SE39EPX", "Aggregate Levy", true, false, "")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_KNOWN_FACTS)
    }
    "return 200 and correct response for successful verification of client case and registration" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("1", "LT", "12LT001", "SE39EP", "Aggregate Levy", true, false, "")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RESPONSE_OK)
    }
    "return 200 and correct response for successful verification of agent case" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("1", "LT", "12LT002", "SE39EX", "Aggregate Levy", true, true, "agent")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RESPONSE_OK)
    }
    "return 200 and error response for unsuccessful verification of client case" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("1", "LT", "12LT032", "SE39EP", "Aggregate Levy", true, false, "")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RESPONSE_NOT_FOUND)
    }
    "return 200 and error response for multiple records found for a given registration number" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("1", "LT", "12LT033", "SE39EP", "Aggregate Levy", true, false, "")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(MULTIPLE_FOUND)
    }
    "return 200 and error response for unsuccessful verification of agent case" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("1", "LT", "12LT002", "SE39EP", "Aggregate Levy", true, true, "agentx")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_ARN)
    }
    "return 200 and correct error response when registration found but for wrong form type" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(RegistrationRequest("2", "LT", "12LT033", "SE39EP", "Aggregate Levy", true, false, "")))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(INCORRECT_REGIME)
    }
  }

  "POST /verify with incorrect request" should {
    "return 400 (BadRequest) and information about errors" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(Json.obj("incorrect" -> "request"))
      val result = TestRegistrationController.register()(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      val errorInformation = jsonBodyOf(await(result))
      val messages: Seq[JsValue] = (errorInformation \\ "msg")
      messages.size must be_>=(1)
    }
  }
}
