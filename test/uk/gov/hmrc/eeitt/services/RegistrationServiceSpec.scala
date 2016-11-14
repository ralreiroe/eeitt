//package uk.gov.hmrc.eeitt.services
//
//import org.specs2.mock.Mockito
//import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
//import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
//import uk.gov.hmrc.eeitt.model.{ RegisterAgentRequest, _ }
//import uk.gov.hmrc.eeitt.repositories._
//import uk.gov.hmrc.play.test.UnitSpec
//import uk.gov.hmrc.eeitt.model.RegistrationResponse._
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//class RegistrationServiceSpec extends UnitSpec
//    with RegistrationRepositorySupport with EtmpAgentRepositorySupport with EtmpBusinessUserRepositorySupport
//    with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience with Mockito {
//
//  object TestRegistrationService extends RegistrationService {
//    val regRepository = mock[MongoRegistrationRepository]
//    regRepository.findRegistrations("1").returns(Future.successful(List(Registration("1", false, "ALLX9876543210123", "", List("LT")))))
//    regRepository.findRegistrations("3").returns(Future.successful(List()))
//    regRepository.findRegistrations("5").returns(Future.successful(List(Registration("5", true, "", "KARN9876543210123", List()))))
//    regRepository.register(RegisterRequest("3", "ALLX9876543210123", "ME1 9AB")).returns(Future.successful(Right(Nil)))
//    regRepository.register(RegisterAgentRequest("3", "KARN9876543210123")).returns(Future.successful(Right(Nil)))
//    regRepository.register(RegisterAgentRequest("5", "KARN9876543210123")).returns(Future.successful(Right(Nil)))
//    regRepository.addRegime(Registration("1", false, "ALLX9876543210123", "", List("LT")), "LX").returns(Future.successful(Right(Nil)))
//    val userRepository = mock[MongoEtmpBusinessUsersRepository]
//    userRepository.userExists(EtmpBusinessUser("ALLX9876543210123", "ME1 9AB")).returns(Future.successful(true))
//    userRepository.userExists(EtmpBusinessUser("ALLX9876543210123", "ME1 9ABX")).returns(Future.successful(false))
//    userRepository.userExists(EtmpBusinessUser("ALLX9876543210124", "ME1 9AB")).returns(Future.successful(true))
//    val agentRepository = mock[MongoEtmpAgentRepository]
//    agentRepository.agentExists(EtmpAgent("KARN9876543210123")).returns(Future.successful(true))
//    agentRepository.agentExists(EtmpAgent("KARN9876543210124")).returns(Future.successful(false))
//    agentRepository.agentExists(EtmpAgent("KARN9876543210125")).returns(Future.successful(true))
//  }
//
//  val service = TestRegistrationService
//
//  override protected def beforeEach(): Unit = {
//    val removeBusinessUsers = agentRepo.removeAll()
//    val removeAgents = userRepo.removeAll()
//    val removeRegistrations = regRepo.removeAll()
//    await(removeAgents)
//    await(removeBusinessUsers)
//    await(removeRegistrations)
//  }
//
//  "Registering a business user with a group id which is not present in repository" should {
//    "affect a new registration record and a 'registration ok' response" in {
//      val response = service.register(RegisterRequest("3", "ALLX9876543210123", "ME1 9AB"))
//      response.futureValue shouldBe RESPONSE_OK
//    }
//  }
//
//  "Registering a business user with a group id which is present in repository" should {
//    "return an error if try to register another business user" in {
//      val response = service.register(RegisterRequest("1", "ALLX9876543210123", "ME1 9AB"))
//      response.futureValue shouldBe ALREADY_REGISTERED
//      //      verify(regRepository.register(RegisterRequest("3", "LX", "AL9876543210123", "ME1 9AB")), atLeastOnce())
//    }
//    "return an error if known facts do not agree with the request" in {
//      val response = service.register(RegisterRequest("3", "ALLX9876543210123", "ME1 9ABX"))
//      response.futureValue shouldBe INCORRECT_KNOWN_FACTS
//    }
//    "affect an updated registration record if the requested regime is not present and known facts agree with the request" in {
//      val response = service.register(RegisterRequest("1", "ALLX9876543210124", "ME1 9AB"))
//      response.futureValue shouldBe RESPONSE_OK
//    }
//    "return an error if already registered to an agent" in {
//      val response = service.register(RegisterRequest("5", "ALLX9876543210123", "ME1 9AB"))
//      response.futureValue shouldBe IS_AGENT
//    }
//  }
//
//  "Registering an agent with a group id which is not present in repository" should {
//    "effect a new registration record and a 'registration ok' response" in {
//      val response = service.register(RegisterAgentRequest("3", "KARN9876543210123"))
//      response.futureValue shouldBe RESPONSE_OK
//    }
//  }
//
//  "Registering an agent with a group id which is present in repository" should {
//    "return success if the group id is already registered" in {
//      val response = service.register(RegisterAgentRequest("5", "KARN9876543210123"))
//      response.futureValue shouldBe ALREADY_REGISTERED
//    }
//    "return an error if try to register another agent" in {
//      val response = service.register(RegisterAgentRequest("5", "KARN9876543210125"))
//      response.futureValue shouldBe RESPONSE_OK
//    }
//    "return an error if already registered to an agent user" in {
//      val response = service.register(RegisterAgentRequest("1", "KARN9876543210123"))
//      response.futureValue shouldBe IS_NOT_AGENT
//    }
//  }
//}
