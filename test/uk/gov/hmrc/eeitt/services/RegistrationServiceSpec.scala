package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.AppendedClues
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceSpec extends UnitSpec with ScalaFutures with AppendedClues {

  def userExists[A](returnValue: Boolean)(checks: A => Unit): UserExists[A] =
    new UserExists[A] {
      def apply(req: A): Future[Boolean] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

  def findRegistration[A, B](returnValue: List[B])(checks: A => Unit): FindRegistration[A, B] =
    new FindRegistration[A, B] {
      def apply(req: A): Future[List[B]] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

  def addRegistration[A](returnValue: Either[String, Unit])(checks: A => Unit): AddRegistration[A] =
    new AddRegistration[A] {
      def apply(req: A): Future[Either[String, Unit]] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

  "Registering a business user with a group id which is not present in repository" should {
    "affect a new registration record and a 'registration ok' response" in {

      val request = RegisterRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), "ME1 9AB")

      implicit val a = userExists(true) { req: RegisterRequest =>
        req should be(request) withClue "in userExists"
      }

      implicit val b = findRegistration(List.empty[IndividualRegistration]) { req: RegisterRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val c = addRegistration(Right(())) { req: RegisterRequest =>
        req should be(request) withClue "in addRegistration"
      }

      val response = RegistrationService.register(request)
      response.futureValue should be(RESPONSE_OK)
    }
  }

  "Registering a business user with a group id which is present in repository" should {
    "return an error if try to register another business user" in {

      val request = RegisterRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), "ME1 9AB")
      val existingRegistration = IndividualRegistration(GroupId(""), RegistrationNumber(""), RegimeId(""))

      implicit val a = userExists(true) { req: RegisterRequest =>
        req should be(request) withClue "in userExists"
      }

      implicit val b = findRegistration(List(existingRegistration)) { req: RegisterRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val c = addRegistration(Right(())) { req: RegisterRequest => /* is not called */ }

      val response = RegistrationService.register(request)
      response.futureValue should be(ALREADY_REGISTERED)
    }

    "return an error if known facts do not agree with the request" in {

      val request = RegisterRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), "ME1 9AB")

      implicit val a = userExists(false) { req: RegisterRequest =>
        req should be(request) withClue "in userExists"
      }

      implicit val b = findRegistration(List.empty[IndividualRegistration]) { req: RegisterRequest => /* is not called */ }
      implicit val c = addRegistration(Right(())) { req: RegisterRequest => /* is not called */ }

      val response = RegistrationService.register(request)
      response.futureValue should be(INCORRECT_KNOWN_FACTS)
    }
  }

  def verificationRepo[A](returnValue: List[A]): VerificationRepo[A] =
    new VerificationRepo[A] {
      def apply(groupId: GroupId, regimeId: RegimeId): Future[List[A]] =
        Future.successful(returnValue)
    }

  val groupId = GroupId("group-id")
  val registrationNumber = RegistrationNumber("123")
  val regimeId = RegimeId("AL")
  val arn = Arn("Arn")
  val individualRegistration = IndividualRegistration(groupId, registrationNumber, regimeId)
  val agentRegistration = AgentRegistration(groupId, arn)

  "Verification of Individual" should {
    "return false when no record is found in db" in {

      implicit val a = verificationRepo(List.empty[IndividualRegistration])

      val response = RegistrationService.verify[IndividualRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(false))
    }

    "return true when exactly one record is found in db" in {

      implicit val a = verificationRepo(List(individualRegistration))

      val response = RegistrationService.verify[IndividualRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(true))
    }

    "return false when more than one record is found in db" in {

      implicit val a = verificationRepo(List(individualRegistration, individualRegistration))

      val response = RegistrationService.verify[IndividualRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(false))
    }
  }

  "Verification of Agent" should {
    "return false when no record is found in db" in {

      implicit val a = verificationRepo(List.empty[AgentRegistration])

      val response = RegistrationService.verify[AgentRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(false))
    }

    "return true when exactly one record is found in db" in {

      implicit val a = verificationRepo(List(agentRegistration))

      val response = RegistrationService.verify[AgentRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(true))
    }

    "return false when more than one record is found in db" in {

      implicit val a = verificationRepo(List(agentRegistration, agentRegistration))

      val response = RegistrationService.verify[AgentRegistration](groupId, regimeId)

      response.futureValue should be(VerificationResponse(false))
    }
  }
}
