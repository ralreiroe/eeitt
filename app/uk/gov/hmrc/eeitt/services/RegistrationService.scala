package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.{ EtmpAgentRepository, EtmpBusinessUsersRepository, RegistrationRepository, etmpBusinessUserRepository, etmpAgentRepository, registrationRepository }
import uk.gov.hmrc.eeitt.model.RegistrationLookupResponse.{ MULTIPLE_FOUND, RESPONSE_NOT_FOUND }
import uk.gov.hmrc.eeitt.model.RegistrationResponse.{ ALREADY_REGISTERED, INCORRECT_KNOWN_FACTS, IS_AGENT, IS_NOT_AGENT, RESPONSE_OK }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationService {

  def registrationRepo: RegistrationRepository
  def userRepository: EtmpBusinessUsersRepository
  def agentRepository: EtmpAgentRepository

  def register(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    verify(registerRequest).flatMap {
      case RESPONSE_OK =>
        registrationRepo.findRegistrations(registerRequest.groupId).flatMap {
          case Nil => addRegistration(registerRequest)
          case x :: Nil => if (x.isAgent) Future.successful(IS_NOT_AGENT) else {
            if (x.registrationNumber == registerRequest.registrationNumber)
              updateRegistration(registerRequest, x)
            else
              Future.successful(INCORRECT_KNOWN_FACTS)
          }
          case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
        }
      case x => Future.successful(x)
    }
  }

  private def verify(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    userRepository.userExists(EtmpBusinessUser(registerRequest.registrationNumber, registerRequest.postcode)).flatMap {
      case true => Future.successful(RESPONSE_OK)
      case false => Future.successful(RegistrationResponse.INCORRECT_KNOWN_FACTS)
    }
  }

  def register(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    verify(registerAgentRequest).flatMap {
      case RESPONSE_OK =>
        registrationRepo.findRegistrations(registerAgentRequest.groupId).flatMap {
          case Nil => addRegistration(registerAgentRequest)
          case x :: Nil => if (x.isAgent) {
            if (x.arn == registerAgentRequest.arn) Future.successful(RESPONSE_OK) else Future.successful(INCORRECT_KNOWN_FACTS)
          } else Future.successful(IS_NOT_AGENT)
          case x :: xs => Future.successful(RegistrationResponse.MULTIPLE_FOUND)
        }
      case x => Future.successful(x)
    }
  }

  private def verify(registerRequest: RegisterAgentRequest): Future[RegistrationResponse] =
    agentRepository.agentExists(EtmpAgent(registerRequest.arn)).flatMap {
      case true => Future.successful(RESPONSE_OK)
      case false => Future.successful(RegistrationResponse.INCORRECT_KNOWN_FACTS)
    }

  private def addRegistration(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    registrationRepo.register(registerRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def addRegistration(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    registrationRepo.registerA(registerAgentRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def updateRegistration(registerRequest: RegisterRequest, registration: Registration): Future[RegistrationResponse] = {
    if (registration.regimeIds.contains(registerRequest.regimeId))
      Future.successful(ALREADY_REGISTERED)
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
      case x :: Nil => RegistrationLookupResponse(None, x.regimeIds)
      case x :: xs => MULTIPLE_FOUND
    }
}

object RegistrationService extends RegistrationService {
  lazy val registrationRepo = registrationRepository
  lazy val userRepository = etmpBusinessUserRepository
  lazy val agentRepository = etmpAgentRepository
}

