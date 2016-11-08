package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationService {

  def regRepository: RegistrationRepository

  def userRepository: EtmpBusinessUsersRepository

  def agentRepository: EtmpAgentRepository

  def register(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    userRepository.userExists(EtmpBusinessUser(registerRequest.registrationNumber, registerRequest.postcode)).flatMap {
      case true =>
        regRepository.findRegistrations(registerRequest.groupId).flatMap {
          case Nil => addRegistration(registerRequest)
          case x :: Nil => x match {
            case Registration(_, false, registerRequest.registrationNumber, _, _) => updateRegistration(registerRequest, x)
            case Registration(_, false, _, _, _) => Future.successful(ALREADY_REGISTERED)
            case Registration(_, true, _, _, _) => Future.successful(IS_AGENT)

          }
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case false => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def register(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    agentRepository.agentExists(EtmpAgent(registerAgentRequest.arn)).flatMap {
      case true =>
        regRepository.findRegistrations(registerAgentRequest.groupId).flatMap {
          case Nil => addRegistration(registerAgentRequest)
          case Registration(_, true, _, registerAgentRequest.arn, _) :: Nil => Future.successful(RESPONSE_OK)
          case Registration(_, true, _, _, _) :: Nil => Future.successful(ALREADY_REGISTERED)
          case Registration(_, false, _, _, _) :: Nil => Future.successful(IS_NOT_AGENT)
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case false => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def verification(groupId: String, regimeId: String): Future[VerificationResponse] = {
    regRepository.findRegistrations(groupId).map {
      case Nil => VerificationResponse(false)
      case Registration(_, false, _, _, y) :: Nil => VerificationResponse(y.contains(regimeId))
      case Registration(_, true, _, _, _) :: Nil => VerificationResponse(true)
      case x :: xs => VerificationResponse(false)
    }
  }

  private def addRegistration(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    regRepository.register(registerRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def addRegistration(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    regRepository.register(registerAgentRequest).flatMap {
      case Right(_) => Future.successful(RESPONSE_OK)
      case Left(x) => Future.successful(RegistrationResponse(Some(x)))
    }
  }

  private def updateRegistration(registerRequest: RegisterRequest, registration: Registration): Future[RegistrationResponse] = {
    if (registration.regimeIds.contains(registerRequest.regimeId))
      Future.successful(RESPONSE_OK)
    else
      regRepository.addRegime(registration, registerRequest.regimeId).flatMap {
        case Right(_) => Future.successful(RESPONSE_OK)
        case Left(x) => Future.successful(RegistrationResponse(Some(x)))
      }
  }
}

object RegistrationService extends RegistrationService {
  lazy val regRepository = registrationRepository
  lazy val userRepository = etmpBusinessUserRepository
  lazy val agentRepository = etmpAgentRepository
}

