package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import uk.gov.hmrc.eeitt.model.{ RegisterAgentRequest, _ }
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends UnitSpec
    with RegistrationRepositorySupport with EtmpAgentRepositorySupport with EtmpBusinessUserRepositorySupport
    with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  object TestRegistrationService extends RegistrationService {
    val userRepository = userRepo
    val agentRepository = agentRepo
    val regRepository = regRepo
  }

  val service = TestRegistrationService

  override protected def beforeEach(): Unit = {
    val removeBusinessUsers = agentRepo.removeAll()
    val removeAgents = userRepo.removeAll()
    val removeRegistrations = regRepo.removeAll()
    await(removeAgents)
    await(removeBusinessUsers)
    await(removeRegistrations)
  }

  "Registering a business user with a group id which is not present in repository" should {
    "effect a new registration record and a 'registration ok' response" in {
      insertBusinessUser(EtmpBusinessUser("AL9876543210123", "ME1 9AB"))
      userRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterRequest("3", "LX", "AL9876543210123", "ME1 9AB"))
      response.futureValue shouldBe RESPONSE_OK
    }
  }

  "Registering a business user with a group id which is present in repository" should {
    "effect an updated registration record if the requested regime is not present and known facts agree with the request" in {
      insertBusinessUser(EtmpBusinessUser("AL9876543210123", "ME1 9AB"))
      userRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", false, "AL9876543210123", "", List("LX")))
      regRepo.count.futureValue shouldBe 1
      await(regRepo.findRegistrations("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX"))
      val response = service.register(RegisterRequest("3", "LT", "AL9876543210123", "ME1 9AB"))
      response.futureValue shouldBe RESPONSE_OK
      await(regRepo.findRegistrations("3")) flatMap (_.regimeIds) should contain theSameElementsAs (List("LX", "LT"))
    }
    "return an error if known facts do not agree with the request" in {
      insertBusinessUser(EtmpBusinessUser("AL9876543210123", "ME1 9AB"))
      userRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", false, "AL9876543210123", "", List("LX")))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterRequest("3", "LX", "AL9876543210123", "ME1 9ABX"))
      response.futureValue shouldBe INCORRECT_KNOWN_FACTS
    }
    "return an error if try to register another business user" in {
      insertBusinessUser(EtmpBusinessUser("AL9876543210123", "ME1 9AB"))
      insertBusinessUser(EtmpBusinessUser("AL9876543210124", "ME1 9AB"))
      userRepo.count.futureValue shouldBe 2
      insertRegistration(Registration("3", false, "AL9876543210123", "", List("LX")))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterRequest("3", "LX", "AL9876543210124", "ME1 9AB"))
      response.futureValue shouldBe ALREADY_REGISTERED
    }
    "return an error if already registered to an agent" in {
      insertBusinessUser(EtmpBusinessUser("AL9876543210123", "ME1 9AB"))
      insertBusinessUser(EtmpBusinessUser("AL9876543210124", "ME1 9AB"))
      userRepo.count.futureValue shouldBe 2
      insertRegistration(Registration("3", true, "", "KARN9876543210123", Seq()))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterRequest("3", "LX", "AL9876543210124", "ME1 9AB"))
      response.futureValue shouldBe IS_AGENT
    }
  }

  "Registering an agent with a group id which is not present in repository" should {
    "effect a new registration record and a 'registration ok' response" in {
      insertAgent(EtmpAgent("KARN9876543210123"))
      agentRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterAgentRequest("3", "KARN9876543210123"))
      response.futureValue shouldBe RESPONSE_OK
    }
  }

  "Registering an agent with a group id which is present in repository" should {
    "return success if the group id is already registered" in {
      insertAgent(EtmpAgent("KARN9876543210123"))
      agentRepo.count.futureValue shouldBe 1
      insertRegistration(Registration("3", true, "", "KARN9876543210123", Seq()))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterAgentRequest("3", "KARN9876543210123"))
      response.futureValue shouldBe RESPONSE_OK
    }
    "return an error if try to register another agent" in {
      insertAgent(EtmpAgent("KARN9876543210123"))
      insertAgent(EtmpAgent("KARN9876543210124"))
      agentRepo.count.futureValue shouldBe 2
      insertRegistration(Registration("3", true, "", "KARN9876543210123", Seq()))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterAgentRequest("3", "KARN9876543210124"))
      response.futureValue shouldBe ALREADY_REGISTERED
    }
    "return an error if already registered to a business user" in {
      insertAgent(EtmpAgent("KARN9876543210123"))
      insertAgent(EtmpAgent("KARN9876543210124"))
      agentRepo.count.futureValue shouldBe 2
      insertRegistration(Registration("3", false, "AL9876543210123", "", List("LX")))
      regRepo.count.futureValue shouldBe 1
      val response = service.register(RegisterAgentRequest("3", "KARN9876543210124"))
      response.futureValue shouldBe IS_NOT_AGENT
    }
  }

}
