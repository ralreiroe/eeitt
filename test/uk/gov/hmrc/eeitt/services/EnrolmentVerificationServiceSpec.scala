package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import uk.gov.hmrc.eeitt.model.RegistrationResponse.{ INCORRECT_ARN, INCORRECT_ARN_FOR_CLIENT, INCORRECT_POSTCODE, INCORRECT_REGIME, MISSING_ARN, RESPONSE_NOT_FOUND, RESPONSE_OK }
import uk.gov.hmrc.eeitt.model.{ Enrolment, RegistrationRequest }
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepositorySupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentVerificationServiceSpec extends UnitSpec with EnrolmentRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestEnrolmentVerificationService extends EnrolmentVerificationService {
    val enrolmentRepo = enroRepo
  }

  val service = TestEnrolmentVerificationService

  override protected def beforeEach(): Unit = {
    await(enroRepo.removeAll())
  }

  "Look up enrolments by registration number" should {
    "produce 'registration number incorrect' response when enrolment is not found" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL1111111111111", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1111111111112", "ME1 9AB", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe RESPONSE_NOT_FOUND
    }

    "produce 'incorrect postcode' response when stored postcode is different than requested postcode" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "N12 6FG", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe INCORRECT_POSTCODE
    }
    "produce 'incorrect postcode' response when stored postcode is the same as requested postcode but lives in the UK flags are different" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Aggregate Levy", false, false, ""))
      response.futureValue shouldBe INCORRECT_POSTCODE
    }
    "produce successful response when stored postcode is different than requested postcode but lives in the UK flag stored is false" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", false, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "N12 6FG", "Aggregate Levy", false, false, ""))
      response.futureValue shouldBe RESPONSE_OK
    }
    "produce successful response when stored postcode is in different format than requested postcode" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "me19ab", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe RESPONSE_OK
    }

    "produce 'regime incorrect' response if stored regime is different than requested regime" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Bingo", true, false, ""))
      response.futureValue shouldBe INCORRECT_REGIME
    }

    "produce successful response when enrolment is found and passed the validation" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe RESPONSE_OK
    }
  }

  "look up enrolments by ARN" should {
    "produce 'registration number incorrect' response when enrolment is not found" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890124", true, "BN1 2AB", "555555555555555"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1111111111111", "BN1 2AB", "Landfill tax", true, true, "555555555555555"))
      response.futureValue shouldBe RESPONSE_NOT_FOUND
    }
    "produce 'incorrect postcode' response when stored postcode is different than requested postcode" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890124", true, "BN1 2AB", "555555555555555"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1234567890124", "BN1 2XX", "Landfill tax", true, true, "555555555555555"))
      response.futureValue shouldBe INCORRECT_POSTCODE
    }
    "produce 'regime incorrect' response if stored regime is different than requested regime" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1234567890123", "BN1 2AB", "Bingo", true, true, "555555555555555"))
      response.futureValue shouldBe INCORRECT_REGIME
    }
    "produce 'ARN incorrect' response when enrolment with a given ARN not found" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1234567890123", "BN1 2AB", "Landfill tax", true, true, "455555555555555"))
      response.futureValue shouldBe INCORRECT_ARN
    }
    "produce 'ARN missing' response when ARN is missing from agent request" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.verify(RegistrationRequest("3", "LT", "AL1234567890123", "BN1 2AB", "Landfill tax", true, true, ""))
      response.futureValue shouldBe MISSING_ARN
    }
    "produce 'ARN exists but not for this client' response when enrolment with a given ARN found for different client" in {
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890123", true, "BN1 2AB", "555555555555555"))
      insertEnrolment(Enrolment("Landfill tax", "AL1234567890124", true, "BN1 2XX", "455555555555555"))
      enroRepo.count.futureValue shouldBe 2
      val response = service.verify(RegistrationRequest("3", "LT", "AL1234567890123", "BN1 2AB", "Landfill tax", true, true, "455555555555555"))
      response.futureValue shouldBe INCORRECT_ARN_FOR_CLIENT
    }
  }
}
