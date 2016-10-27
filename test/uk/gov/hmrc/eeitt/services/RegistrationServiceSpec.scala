package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import uk.gov.hmrc.eeitt.model.{ Registration, RegistrationRequest }
import uk.gov.hmrc.eeitt.repositories.RegistrationRepositorySupport
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends UnitSpec with RegistrationRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestRegistrationService extends RegistrationService {
    val registrationRepo = repo
  }

  val service = TestRegistrationService

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "Registering with a group id which is not present in repository" should {
    "effect a new registration record and a 'registration ok' response" in {
      val response = service.register(RegistrationRequest("3", "LT", "12LT009", "SE39EP"))
      response.futureValue shouldBe REGISTRATION_OK
    }
  }

  "Registering with a group id which is present in repository" should {
    "effect an updated registration record if the requested regime is not present and known facts agree with the request" in {
      insertRegistration(Registration("3", List("LX"), "12LT009", "SE39EP"))
      repo.count.futureValue shouldBe 1
      await(repo.lookupRegistration("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX"))
      val response = service.register(RegistrationRequest("3", "LT", "12LT009", "SE39EP"))
      response.futureValue shouldBe REGISTRATION_OK
      await(repo.lookupRegistration("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX", "LT"))
    }
    "return an error if known facts do not agree with the request" in {
      insertRegistration(Registration("3", List("LX"), "12LT009", "SE39EP"))
      repo.count.futureValue shouldBe 1
      val response = service.register(RegistrationRequest("3", "LT", "12LT009", "SE39EX"))
      response.futureValue shouldBe INCORRECT_KNOWN_FACTS
    }
    "return an error if the group id is already registered" in {
      insertRegistration(Registration("3", List("LT"), "12LT009", "SE39EP"))
      repo.count.futureValue shouldBe 1
      val response = service.register(RegistrationRequest("3", "LT", "12LT009", "SE39EP"))
      response.futureValue shouldBe ALREADY_REGISTERED
    }
  }

}
