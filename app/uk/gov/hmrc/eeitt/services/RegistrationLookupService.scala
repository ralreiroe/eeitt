package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse
import uk.gov.hmrc.eeitt.repositories.{ RegistrationRepository, registrationRepository }
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ RESPONSE_NOT_FOUND, MULTIPLE_FOUND }

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationLookupService {

  def registrationRepo: RegistrationRepository

  def lookup(groupId: String): Future[RegistrationLookupResponse] =
    registrationRepo.lookupRegistration(groupId).map {
      case Nil => RESPONSE_NOT_FOUND
      case x :: Nil => RegistrationLookupResponse(None, Some(x))
      case x :: xs => MULTIPLE_FOUND
    }

  def check(groupId: String, regimeId: String): Future[RegistrationLookupResponse] =
    registrationRepo.check(groupId, regimeId).map {
      case Nil => RESPONSE_NOT_FOUND
      case x :: Nil => RegistrationLookupResponse(None, Some(x))
      case x :: xs => MULTIPLE_FOUND
    }
}

object RegistrationLookupService extends RegistrationLookupService {
  lazy val registrationRepo = registrationRepository
}

