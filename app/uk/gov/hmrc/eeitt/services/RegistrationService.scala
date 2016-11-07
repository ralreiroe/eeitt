package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ RegisterAgentRequest, RegisterRequest, Registration, RegistrationLookupResponse, RegistrationResponse, VerificationResponse }
import uk.gov.hmrc.eeitt.repositories.{ RegistrationRepository, registrationRepository }
import uk.gov.hmrc.eeitt.model.VerificationResponse
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ MULTIPLE_FOUND, RESPONSE_NOT_FOUND }
import uk.gov.hmrc.eeitt.model.RegistrationResponse.{ ALREADY_REGISTERED, INCORRECT_KNOWN_FACTS, RESPONSE_OK, IS_AGENT, IS_NOT_AGENT }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationService {

  def registrationRepo: RegistrationRepository

  def register(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    EnrolmentVerificationService.verify(registerRequest).flatMap {
      case RESPONSE_OK =>
        registrationRepo.findRegistrations(registerRequest.groupId).flatMap {
          case Nil => addRegistration(registerRequest)
          case x :: Nil => if (x.isAgent) Future.successful(IS_NOT_AGENT) else updateRegistration(registerRequest, x)
          case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
        }
      case x => Future.successful(x)
    }
  }

  def registerAgent(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    EnrolmentVerificationService.verifyAgent(registerAgentRequest).flatMap {
      case RESPONSE_OK =>
        registrationRepo.findRegistrations(registerAgentRequest.groupId).flatMap {
          case Nil => addAgentRegistration(registerAgentRequest)
          case x :: Nil => if (x.isAgent) Future.successful(RegistrationResponse.RESPONSE_OK) else Future.successful(IS_AGENT)
          case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
        }
      case x => Future.successful(x)
    }
  }

  private def addRegistration(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    registrationRepo.register(registerRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def addAgentRegistration(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    registrationRepo.registerA(registerAgentRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def updateRegistration(registerRequest: RegisterRequest, registration: Registration): Future[RegistrationResponse] = {
    if (registration.regimeIds.contains(registerRequest.regimeId))
      Future.successful(RESPONSE_OK)
    else
      registrationRepo.addRegime(registration, registerRequest.regimeId).flatMap {
        case Right(_) => Future.successful(RESPONSE_OK)
        case Left(x) => Future.successful(RegistrationResponse(Some(x)))
      }
  }

  def verification(groupId: String, regimeId: String): Future[VerificationResponse] =
    registrationRepo.findRegistrations(groupId).map {
      case Nil => VerificationResponse(false)
      case x :: Nil =>
        val z: VerificationResponse = x match {
          case Registration(_, false, _, _, y) => VerificationResponse(y.contains(regimeId))
          case Registration(_, true, _, _, _) => VerificationResponse(true)
        }
        z
      case x :: xs => VerificationResponse(false)
    }

  def lookup(groupId: String): Future[RegistrationLookupResponse] =
    registrationRepo.findRegistrations(groupId).map {
      case Nil => RESPONSE_NOT_FOUND
      case x :: Nil => { RegistrationLookupResponse(None, x.regimeIds) }
      case x :: xs => MULTIPLE_FOUND
    }

  def registerVerified(registrationRequest: RegisterRequest): Future[RegistrationResponse] = {
    registrationRepo.findRegistrations(registrationRequest.groupId).flatMap {
      case Nil => addRegistration(registrationRequest)
      case x :: Nil => updateRegistration(registrationRequest, x)
      case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
    }
  }

//  object DifferentKnownFacts {
//    def unapply(p: (RegisterRequest, Registration)): Boolean = p match {
//      case (rr, r) => rr.registrationNumber != r.registrationNumber || norm(rr.postcode) != norm(r.postcode)
//    }
//    private def norm(p: String) = p.trim.toUpperCase.replaceAll("\\s", "")
//  }
//
//  object RegimePresent {
//    def unapply(p: (RegisterRequest, Registration)): Boolean = p match {
//      case (rr, r) => r.regimeIds.contains(rr.regimeId)
//    }
//  }
}

object RegistrationService extends RegistrationService {
  lazy val registrationRepo = registrationRepository
}

