package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import uk.gov.hmrc.eeitt.model.{ Enrolment, Registration, RegistrationRequest }
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepositorySupport
import uk.gov.hmrc.eeitt.repositories.RegistrationRepositorySupport
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends UnitSpec with RegistrationRepositorySupport with EnrolmentRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestRegistrationService extends RegistrationService {
    val enrolmentRepo = enroRepo
    val registrationRepo = regRepo
  }

  val service = TestRegistrationService

  override protected def beforeEach(): Unit = {
    val removeEnrolments = enroRepo.removeAll()
    val removeRegistrations = regRepo.removeAll()
    await(removeEnrolments)
    await(removeRegistrations)
  }

  //    case class RegistrationRequest(groupId: String, regimeId: String, registrationNumber: String, postcode: String,
  //                                   formTypeRef: String, livesInTheUk: Boolean, isAgent: Boolean, arn: String)

  // case class EnrolmentVerificationRequest(formTypeRef: String, registrationNumber: String, livesInTheUk: Boolean, postcode: String, isAgent: Boolean, arn: String)

  "Registering with a group id which is not present in repository" should {
    "effect a new registration record and a 'registration ok' response" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      val response = service.register(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe RESPONSE_OK
    }
  }

  "Registering with a group id which is present in repository" should {
    "effect an updated registration record if the requested regime is not present and known facts agree with the request" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", List("LX"), "AL9876543210123", "ME1 9AB"))
      regRepo.count.futureValue shouldBe 1
      await(regRepo.findRegistrations("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX"))
      val response = service.register(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe RESPONSE_OK
      await(regRepo.findRegistrations("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX", "LT"))
    }
    "return an error if known facts do not agree with the request" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", List("LX"), "AL9876543210123", "ME1 9AB"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.register(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9ABX", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe INCORRECT_KNOWN_FACTS
    }
    "return an error if the group id is already registered" in {
      insertEnrolment(Enrolment("Aggregate Levy", "AL9876543210123", true, "ME1 9AB", ""))
      enroRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", List("LT"), "AL9876543210123", "ME1 9AB"))
      enroRepo.count.futureValue shouldBe 1
      val response = service.register(RegistrationRequest("3", "LT", "AL9876543210123", "ME1 9AB", "Aggregate Levy", true, false, ""))
      response.futureValue shouldBe ALREADY_REGISTERED
    }
  }

}
