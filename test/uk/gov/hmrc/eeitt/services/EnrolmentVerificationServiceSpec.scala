package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ INCORRECT_ARN, INCORRECT_ARN_FOR_CLIENT, INCORRECT_POSTCODE, INCORRECT_REGIME, MISSING_ARN, RESPONSE_NOT_FOUND, RESPONSE_OK }
import uk.gov.hmrc.eeitt.model.{ Enrolment, EnrolmentVerificationRequest, EnrolmentVerificationResponse }
import uk.gov.hmrc.eeitt.repositories.RepositorySupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentVerificationServiceSpec extends UnitSpec with RepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestEnrolmentVerificationService extends EnrolmentVerificationService {
    val enrolmentRepo = repo
  }

  val service = TestEnrolmentVerificationService

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "Look up enrolments by registration number" should {

    "produce 'registration number incorrect' response when enrolment is not found" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL1111111111111", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL1111111111112", true, "ME1 9AB", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_NOT_FOUND)
    }

    "produce 'incorrect postcode' response when stored postcode is different than requested postcode" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "N12 6FG", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_POSTCODE)
    }
    "produce 'incorrect postcode' response when stored postcode is the same as requested postcode but lives in the UK flags are different" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", false, "ME1 9AB", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_POSTCODE)
    }
    "produce successful response when stored postcode is different than requested postcode but lives in the UK flag stored is false" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", false, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", false, "N12 6FG", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_OK)
    }
    "produce successful response when stored postcode is in different format than requested postcode" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "me19ab", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_OK)
    }

    "produce 'regime incorrect' response if stored regime is different than requested regime" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Bingo", "AL9876543210123", true, "ME1 9AB", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_REGIME)
    }

    "produce successful response when enrolment is found and passed the validation" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", false, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_OK)
    }
  }

  "look up enrolments by ARN" should {
    "produce 'registration number incorrect' response when enrolment is not found" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Landfill tax", "AL1234567890124", true, "BN1 2AB", true, "555555555555555"))
      response.futureValue shouldBe EnrolmentVerificationResponse(RESPONSE_NOT_FOUND)
    }
    "produce 'incorrect postcode' response when stored postcode is different than requested postcode" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Landfill tax", "AL1234567890123", true, "BN1 2XX", true, "555555555555555"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_POSTCODE)
    }
    "produce 'regime incorrect' response if stored regime is different than requested regime" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Bingo", "AL1234567890123", true, "BN1 2AB", true, "555555555555555"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_REGIME)
    }
    "produce 'ARN incorrect' response when enrolment with a given ARN not found" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Landfill tax", "AL1234567890123", true, "BN1 2AB", true, "455555555555555"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_ARN)
    }
    "produce 'ARN missing' response when ARN is missing from agent request" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      repo.count.futureValue shouldBe 1
      val response = service.verify(EnrolmentVerificationRequest("Landfill tax", "AL1234567890123", true, "BN1 2AB", true, ""))
      response.futureValue shouldBe EnrolmentVerificationResponse(MISSING_ARN)
    }
    "produce 'ARN exists but not for this client' response when enrolment with a given ARN found for different client" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890124", true, "BN1 2XX", "455555555555555"))
      repo.count.futureValue shouldBe 2
      val response = service.verify(EnrolmentVerificationRequest("Landfill tax", "AL1234567890123", true, "BN1 2AB", true, "455555555555555"))
      response.futureValue shouldBe EnrolmentVerificationResponse(INCORRECT_ARN_FOR_CLIENT)
    }
  }
}
