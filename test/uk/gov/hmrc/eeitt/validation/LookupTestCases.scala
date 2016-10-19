package uk.gov.hmrc.eeitt.validation

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import uk.gov.hmrc.eeitt.RepositorySupport
import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ INCORRECT_POSTCODE, INCORRECT_REGIME, RESPONSE_NOT_FOUND, RESPONSE_OK }
import uk.gov.hmrc.eeitt.model.{ Enrolment, EnrolmentVerificationRequest, EnrolmentVerificationResponse }
import uk.gov.hmrc.eeitt.services.EnrolmentVerificationService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by harrison on 17/10/16.
 * Extended by milosz on 18/10/16.
 */
class LookupTestCases extends UnitSpec with RepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestEnrolmentVerificationService extends EnrolmentVerificationService {
    val enrolmentRepo = repo
  }

  val service = TestEnrolmentVerificationService

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "Look up enrollments by registration number" should {

    /**
     * Cases 7 and 11
     */
    "produce 'registration number incorrect' response when enrolment is not found" in {
      insertEnrolment(Enrolment(fakeId, "Aggregate Levy", "AL1111111111111", true, "ME1 9AB"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL1111111111112", true, "ME1 9AB"))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_NOT_FOUND)
    }

    /**
     * Cases 8 and 12
     */
    "produce 'incorrect postcode' response when stored postcode is different than requested postcode" in {
      insertEnrolment(Enrolment(fakeId, "Aggregate Levy", "AL9876543210123", true, "ME1 9AB"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "N12 6FG"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_POSTCODE)
    }
    "produce successful response when stored postcode is in different format than requested postcode" in {
      insertEnrolment(Enrolment(fakeId, "Aggregate Levy", "AL9876543210123", true, "ME1 9AB"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "me19ab"))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_OK)
    }

    /**
     * Cases 9 and 13
     */
    "produce 'regime incorrect' response if stored regime is different than requested regime" in {
      insertEnrolment(Enrolment(fakeId, "Aggregate Levy", "AL9876543210123", true, "ME1 9AB"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Bingo", "AL9876543210123", true, "ME1 9AB"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_REGIME)
    }

    /**
     * Cases 10 and 14
     */
    "produce successful response when enrolment is found and passed the validation" in {
      insertEnrolment(Enrolment(fakeId, "Aggregate Levy", "AL9876543210123", true, "ME1 9AB"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "ME1 9AB"))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_OK)
    }
  }
}
