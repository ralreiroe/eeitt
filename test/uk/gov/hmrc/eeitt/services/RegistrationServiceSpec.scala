package uk.gov.hmrc.eeitt.services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.AppendedClues
import org.scalatest.time.{ Span, Millis }
import uk.gov.hmrc.eeitt.{ EtmpFixtures, TypeclassFixtures }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.utils.CountryCodes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceSpec extends UnitSpec with ScalaFutures with AppendedClues with EtmpFixtures with TypeclassFixtures {

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(500, Millis)), interval = scaled(Span(150, Millis)))

  "Registering a business user with a group id which is not present in repository" should {
    "affect a new registration record and a 'registration ok' response" in {

      val request = RegisterBusinessUserRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), Some(Postcode("BN12 4XL")))

      implicit val a = findUser(List(testEtmpBusinessUser())) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty[RegistrationBusinessUser]) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findRegistration"
      }

      implicit val c = addRegistration(Right(())) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in addRegistration"
      }

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

      implicit val c = addRegistration(Right(())) { req: RegisterBusinessUserRequest => /* is not called */ }

      val response = RegistrationService.register[RegisterBusinessUserRequest, EtmpBusinessUser](request)
      response.futureValue should be(ALREADY_REGISTERED)
    }

    "return an error if known facts do not agree with the request" in {

      val request = RegisterBusinessUserRequest(GroupId("3"), RegistrationNumber("ALLX9876543210123"), Some(Postcode("ME1 9AB")))

      implicit val a = findUser(List.empty[EtmpBusinessUser]) { req: RegisterBusinessUserRequest =>
        req should be(request) withClue "in findUser"
      }

      implicit val b = findRegistration(List.empty[RegistrationBusinessUser]) { req: RegisterBusinessUserRequest => /* is not called */ }
      implicit val c = addRegistration(Right(())) { req: RegisterBusinessUserRequest => /* is not called */ }

      val response = RegistrationService.register[RegisterBusinessUserRequest, EtmpBusinessUser](request)
      response.futureValue should be(INCORRECT_KNOWN_FACTS_BUSINESS_USERS)
    }
  }
}
