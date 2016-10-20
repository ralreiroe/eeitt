package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ INCORRECT_REGIME, RESPONSE_NOT_FOUND, RESPONSE_OK }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository
import uk.gov.hmrc.eeitt.services.EnrolmentVerificationService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

import scala.concurrent.Future

class EnrolmentControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  val fakeId = BSONObjectID.generate

  object TestEnrolmentStoreService extends EnrolmentVerificationService {
    val enrolmentRepo = mock[EnrolmentRepository]
    enrolmentRepo.lookupEnrolment("foo").returns(Future.successful(List(Enrolment(fakeId, "1", "foo", true, "SE39EP", ""))))
    enrolmentRepo.lookupEnrolment("12LT32").returns(Future.successful(List()))
  }

  object TestEnrolmentController extends EnrolmentController {
    val enrolmentVerificationService = TestEnrolmentStoreService
  }

  "POST /verify" should {
    "return 200 and correct response for successful verification" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(EnrolmentVerificationRequest("1", "foo", true, "SE39EP", false, "")))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(EnrolmentVerificationResponse(RESPONSE_OK))
    }
    "return 200 and error response for unsuccessful verification" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(EnrolmentVerificationRequest("1", "12LT32", true, "SE39EP", false, "")))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(EnrolmentVerificationResponse(RESPONSE_NOT_FOUND))
    }
    "return 200 and correct error response when registration found but for wrong form type" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(EnrolmentVerificationRequest("2", "foo", true, "SE39EP", false, "")))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(EnrolmentVerificationResponse(INCORRECT_REGIME))
    }
  }

  "POST /verify with incorrect request" should {
    "return 400 (BadRequest) and information about errors" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(Json.obj("incorrect" -> "request"))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      val errorInformation = jsonBodyOf(await(result))
      val messages: Seq[JsValue] = (errorInformation \\ "msg")
      messages.size must be_>=(1)
    }
  }
}
