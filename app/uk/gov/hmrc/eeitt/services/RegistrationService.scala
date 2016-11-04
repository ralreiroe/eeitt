package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ VerificationResponse$, RegistrationLookupResponse, RegistrationRequest, RegistrationResponse, Registration }
import uk.gov.hmrc.eeitt.repositories.{ RegistrationRepository, registrationRepository }
import uk.gov.hmrc.eeitt.model.VerificationResponse
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ MULTIPLE_FOUND, RESPONSE_NOT_FOUND }
import uk.gov.hmrc.eeitt.model.RegistrationResponse.{ ALREADY_REGISTERED, RESPONSE_OK, INCORRECT_KNOWN_FACTS }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationService {

  def registrationRepo: RegistrationRepository

  def verification(groupId: String, regimeId: String): Future[VerificationResponse] =
    registrationRepo.findRegistrations(groupId).map {
      case Nil => VerificationResponse(false)
      case x :: Nil =>
        val z: VerificationResponse = x match {
          case Registration(_, _, false, _, _, y) => VerificationResponse(y.contains(regimeId))
          case Registration(_, _, true, _, _, _) => VerificationResponse(true)
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

  def register(registrationRequest: RegistrationRequest): Future[RegistrationResponse] = {
    registrationRepo.findRegistrations(registrationRequest.groupId).flatMap {
      case Nil => doRegister(registrationRequest)
      case x :: Nil => verifyOrAddRegime(registrationRequest, x)
      case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
    }
  }

  def registerVerified(registrationRequest: RegistrationRequest): Future[RegistrationResponse] = {
    registrationRepo.findRegistrations(registrationRequest.groupId).flatMap {
      case Nil => doRegister(registrationRequest)
      case x :: Nil => verifyOrAddRegime(registrationRequest, x)
      case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
    }
  }

  private def verifyOrAddRegime(request: RegistrationRequest, registration: Registration): Future[RegistrationResponse] = {
    (request, registration) match {
      case DifferentKnownFacts() => Future.successful(INCORRECT_KNOWN_FACTS)
      case RegimePresent() => Future.successful(RESPONSE_OK)
      case (rr, r) => addRegime(r, rr.regimeId)
    }
  }

  private def addRegime(registration: Registration, regimeId: String): Future[RegistrationResponse] = {
    registrationRepo.addRegime(registration, regimeId) map {
      case Right(r) => RESPONSE_OK
      case Left(error) => RegistrationResponse(Some(error))
    }
  }

  private def doRegister(registrationRequest: RegistrationRequest): Future[RegistrationResponse] = {
    registrationRepo.register(registrationRequest) map {
      case Right(r) => RESPONSE_OK
      case Left(error) => RegistrationResponse(Some(error))
    }
  }

  object DifferentKnownFacts {
    def unapply(p: (RegistrationRequest, Registration)): Boolean = p match {
      case (rr, r) => rr.registrationNumber != r.registrationNumber || norm(rr.postcode) != norm(r.postcode)
    }
    private def norm(p: String) = p.trim.toUpperCase.replaceAll("\\s", "")
  }

  object RegimePresent {
    def unapply(p: (RegistrationRequest, Registration)): Boolean = p match {
      case (rr, r) => r.regimeIds.contains(rr.regimeId)
    }
  }
}

object RegistrationService extends RegistrationService {
  lazy val registrationRepo = registrationRepository
}

