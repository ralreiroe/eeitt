package uk.gov.hmrc.eeitt.controllers

import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ Enrolment, EnrolmentResponseNotFound, EnrolmentResponseOk, EnrolmentVerificationRequest }
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository
import uk.gov.hmrc.eeitt.services.EnrolmentStoreService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

import scala.concurrent.Future

class EnrolmentControllerSpec extends UnitSpec with WithFakeApplication with Mockito {

  val fakeId = BSONObjectID.generate

  object TestEnrolmentStoreService extends EnrolmentStoreService {
    val enrolmentRepo = mock[EnrolmentRepository]
    enrolmentRepo.getAllEnrolments().returns(Future.successful(List(Enrolment(fakeId, "1", "12LT31", true, "SE39EP"))))
    enrolmentRepo.lookupEnrolment("12LT31").returns(Future.successful(List(Enrolment(fakeId, "1", "12LT31", true, "SE39EP"))))
    enrolmentRepo.lookupEnrolment("12LT32").returns(Future.successful(List()))
  }

  object TestEnrolmentController extends EnrolmentController {
    val enrolmentStoreService = TestEnrolmentStoreService
  }

  "GET /enrolments" should {
    "return 200" in {
      val fakeRequest = FakeRequest("GET", "/enrolments")
      val result = TestEnrolmentController.enrolments()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "POST /verify" should {
    "return 200 and correct response for successful verification" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(EnrolmentVerificationRequest("1", "12LT31", true, "SE39EP")))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(EnrolmentResponseOk)
    }
  }

  "POST /verify" should {
    "return 200 and error response for unsuccessful verification" in {
      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(toJson(EnrolmentVerificationRequest("1", "12LT32", true, "SE39EP")))
      val result = TestEnrolmentController.verify()(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(EnrolmentResponseNotFound)
    }
  }

}
