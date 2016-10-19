package uk.gov.hmrc.eeitt.validation

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{RESPONSE_OK,RESPONSE_NOT_FOUND,INCORRECT_POSTCODE,INCORRECT_REGIME}
import uk.gov.hmrc.eeitt.model.{ Enrolment, EnrolmentVerificationRequest, EnrolmentVerificationResponse }
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository
import uk.gov.hmrc.eeitt.services.EnrolmentVerificationService
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by harrison on 17/10/16.
 * Exyended by milosz on 18/10/16.
 */
class LookupTestCases extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  val repo = new EnrolmentRepository
  val fakeId = BSONObjectID.generate

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
    "produce wrong postcode response when stored postcode is different than requested postcode" in {
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

  def insertEnrolment(enrolment: Enrolment): BSONObjectID = {
    val lease = Enrolment(_id = BSONObjectID.generate, enrolment.formTypeRef, enrolment.registrationNumber, enrolment.livesInTheUk, enrolment.postcode)
    await(repo.collection.insert(lease))
    lease._id
  }
}
