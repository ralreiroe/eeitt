package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.AppendedClues
import org.scalatest.time.{ Span, Millis }
import reactivemongo.api.commands.MultiBulkWriteResult
import uk.gov.hmrc.eeitt.{ EtmpFixtures, RegistrationFixtures, TypeclassFixtures }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.utils.CountryCodes
import uk.gov.hmrc.eeitt.repositories.RegistrationAgentRepository
import uk.gov.hmrc.eeitt.repositories.RegistrationRepository
import uk.gov.hmrc.eeitt.repositories.EtmpAgentRepository
import uk.gov.hmrc.eeitt.repositories.EtmpBusinessUsersRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceSpec extends UnitSpec with ScalaFutures with AppendedClues with EtmpFixtures with RegistrationFixtures with TypeclassFixtures {

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(500, Millis)), interval = scaled(Span(150, Millis)))

  "Registering a business user with a group id which is not present in repository" should {
    "return a 'registration ok' response" in {

      val request = RegisterBusinessUserRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), Some(Postcode("BN12 4XL")))

      implicit val a = findUser(List(testEtmpBusinessUser())) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty[RegistrationBusinessUser]) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val addRegistration = (_: RegisterBusinessUserRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterBusinessUserRequest, EtmpBusinessUser](request)
      response.futureValue should be(RESPONSE_OK)
    }
  }

  "Registering a business user with a group id which is present in repository" should {
    "return an error if try to register another business user" in {

      val request = RegisterBusinessUserRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), Some(Postcode("BN12 4XL")))
      val existingRegistration = RegistrationBusinessUser(GroupId(""), RegistrationNumber(""), RegimeId(""))

      implicit val a = findUser(List(testEtmpBusinessUser())) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List(existingRegistration)) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findRegistration"
      }
      implicit val addRegistration = (_: RegisterBusinessUserRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterBusinessUserRequest, EtmpBusinessUser](request)
      response.futureValue should be(ALREADY_REGISTERED)
    }

    "return an error if known facts do not agree with the request" in {

      val request = RegisterBusinessUserRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), Some(Postcode("ME1 9AB")))

      implicit val a = findUser(List.empty[EtmpBusinessUser]) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty[RegistrationBusinessUser]) { req: RegisterBusinessUserRequest => /* is not called */ }
      implicit val addRegistration = (_: RegisterBusinessUserRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterBusinessUserRequest, EtmpBusinessUser](request)
      response.futureValue should be(INCORRECT_KNOWN_FACTS_BUSINESS_USERS)
    }
  }

  "Registering a agent with a group id which is present in repository" should {
    "return an error if try to register another agent" in {

      val request = RegisterAgentRequest(GroupId("3"), Arn("ALLX9876543210123"), Some(Postcode("BN12 4XL")))
      val existingRegistration = RegistrationAgent(GroupId(""), Arn(""))

      implicit val a = findUser(List(testEtmpAgent())) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List(existingRegistration)) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val addRegistration = (_: RegisterAgentRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterAgentRequest, EtmpAgent](request)
      response.futureValue should be(ALREADY_REGISTERED)
    }

    "return an error if known facts do not agree with the request" in {

      val request = RegisterAgentRequest(GroupId("3"), Arn("ALLX9876543210123"), Some(Postcode("ME1 9AB")))

      implicit val a = findUser(List.empty[EtmpAgent]) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty[RegistrationAgent]) { req: RegisterAgentRequest => /* is not called */ }
      implicit val addRegistration = (_: RegisterAgentRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterAgentRequest, EtmpAgent](request)
      response.futureValue should be(INCORRECT_KNOWN_FACTS_AGENTS)
    }

    "return an error if adding new registration fails" in {

      val request = RegisterAgentRequest(GroupId("3"), Arn("ALLX9876543210123"), Some(Postcode("BN12 4XL")))

      implicit val a = findUser(List(testEtmpAgent())) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val addRegistration = (_: RegisterAgentRequest) => Future.successful(Left("failed-to-add-registration"))

      val response = RegistrationService.register[RegisterAgentRequest, EtmpAgent](request)
      response.futureValue should be(RegistrationResponse(Some("failed-to-add-registration")))
    }

    "return an error if multiple registration are found" in {

      val request = RegisterAgentRequest(GroupId("3"), Arn("ALLX9876543210123"), Some(Postcode("BN12 4XL")))

      val existingRegistration = RegistrationAgent(GroupId(""), Arn(""))

      implicit val a = findUser(List(testEtmpAgent())) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List(existingRegistration, existingRegistration)) { req: RegisterAgentRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val addRegistration = (_: RegisterAgentRequest) => Future.successful(Right(()))

      val response = RegistrationService.register[RegisterAgentRequest, EtmpAgent](request)
      response.futureValue should be(MULTIPLE_FOUND)
    }
  }

  "Show type class" should {
    "provide text representation for GroupId value class" in {
      val show = implicitly[Show[GroupId]]
      show(GroupId("g")) should be("GroupId(g)")
    }
    "provide text representation for tuple of value class GroupId and RegimeId" in {
      val show = implicitly[Show[(GroupId, RegimeId)]]
      show((GroupId("g"), RegimeId("r"))) should be("(GroupId(g), RegimeId(r))")
    }
  }

  "the addRegistration function" should {

    "be defined for RegisterAgentRequest, and should return the register method in the implicit  RegistrationAgentRepository returns" in {
      implicit val registrationAgentRepo = new RegistrationAgentRepository {
        def findRegistrations(groupId: GroupId): Future[List[RegistrationAgent]] = ???
        def register(rr: RegisterAgentRequest): Future[Either[String, Unit]] = Future.successful(Left("something returned by RegistrationAgentRepository"))
      }
      val request = RegisterAgentRequest(GroupId(""), Arn(""), Some(Postcode("")))
      uk.gov.hmrc.eeitt.implicits.addReg(request).futureValue should be(Left("something returned by RegistrationAgentRepository"))
    }

    "be defined for RegisterBusinessUserRequest, and should return the register method in the implicit RegistrationRepository returns" in {
    "be defined for RegisterBusinessUserRequest, and should return the register method in the implicit RegistrationRepository returns" in {
      implicit val registrationRepo = new RegistrationRepository {
        def findRegistrations(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationBusinessUser]] = ???
        def register(rr: RegisterBusinessUserRequest): Future[Either[String, Unit]] = Future.successful(Right(()))
      }
      val addRegistration = implicitly[AddRegistration[RegisterBusinessUserRequest]]

      val request = RegisterBusinessUserRequest(GroupId(""), RegistrationNumber("ALLX9876543210123"), Some(Postcode("")))
      addRegistration(request).futureValue should be(Right(()))
    }
  }

  "FindUser type class" should {
    "be defined for [RegisterAgentRequest, EtmpAgent] if there exists implicit value of EtmpAgentRepository" in {
      implicit val etmpAgentRepo = new EtmpAgentRepository {
        def findByArn(arn: Arn): Future[List[EtmpAgent]] = Future.successful(List.empty[EtmpAgent])
        def replaceAll(users: Seq[EtmpAgent]): Future[MultiBulkWriteResult] = ???
      }

      val findUser = implicitly[FindUser[RegisterAgentRequest, EtmpAgent]]
      val request = RegisterAgentRequest(GroupId(""), Arn(""), Some(Postcode("")))
      findUser(request).futureValue should be(List.empty[EtmpAgent])
    }

    "be defined for [RegisterBusinessUserRequest, EtmpBusinessUser] if there exists implicit value of EtmpBusinessUsersRepository" in {
      implicit val etmpAgentRepo = new EtmpBusinessUsersRepository {
        def findByRegistrationNumber(registrationNumber: RegistrationNumber): Future[List[EtmpBusinessUser]] = Future.successful(List.empty[EtmpBusinessUser])
        def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult] = ???
      }

      val findUser = implicitly[FindUser[RegisterBusinessUserRequest, EtmpBusinessUser]]
      val request = RegisterBusinessUserRequest(GroupId(""), RegistrationNumber("ALLX9876543210123"), Some(Postcode("")))
      findUser(request).futureValue should be(List.empty[EtmpBusinessUser])
    }
  }

  "FindRegistration type class" should {

    implicit val registrationRepo = new RegistrationRepository {
      def findRegistrations(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationBusinessUser]] = Future.successful(List(testRegistrationBusinessUser()))
      def register(rr: RegisterBusinessUserRequest): Future[Either[String, Unit]] = ???
    }

    implicit val registrationAgentRepo = new RegistrationAgentRepository {
      def findRegistrations(groupId: GroupId): Future[List[RegistrationAgent]] = Future.successful(List(testRegistrationAgent()))
      def register(rr: RegisterAgentRequest): Future[Either[String, Unit]] = ???
    }

    "be defined for RegisterBusinessUserRequest if there exists implicit value of RegistrationRepository" in {
      val findRegistration = implicitly[FindRegistration[RegisterBusinessUserRequest]]

      val request = RegisterBusinessUserRequest(GroupId(""), RegistrationNumber("ALLX9876543210123"), Some(Postcode("")))
      findRegistration(request).futureValue should have size (1)
    }

    "be defined for tuple of GroupId and RegimeId if there exists implicit value of RegistrationRepository" in {
      val findRegistration = implicitly[FindRegistration[(GroupId, RegimeId)]]

      val request = (GroupId("g"), RegimeId("r"))
      findRegistration(request).futureValue should have size (1)
    }

    "be defined for RegisterAgentRequest if there exists implicit value of RegistrationAgentRepository" in {
      val findRegistration = implicitly[FindRegistration[RegisterAgentRequest]]

      val request = RegisterAgentRequest(GroupId(""), Arn(""), Some(Postcode("")))
      findRegistration(request).futureValue should have size (1)
    }

    "be defined for GroupId if there exists implicit value of RegistrationAgentRepository" in {
      val findRegistration = implicitly[FindRegistration[GroupId]]

      val request = GroupId("g")
      findRegistration(request).futureValue should have size (1)
    }
  }
}
