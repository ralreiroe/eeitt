package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.PostcodeValidator._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RegistrationService {

  def regRepository: RegistrationRepository

  def userRepository: EtmpBusinessUsersRepository

  def agentRepository: EtmpAgentRepository

  def register(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    userRepository.findByRegistrationNumber(registerRequest.registrationNumber).flatMap {
      case user :: maybeOtherUsers if postcodeValidOrNotNeeded(user, registerRequest.postcode) =>
        regRepository.findRegistrations(registerRequest.groupId).flatMap {
          case Nil => addRegistration(registerRequest)
          case x :: Nil => x match {
            case Registration(_, false, registerRequest.registrationNumber, _, _) => Future.successful(ALREADY_REGISTERED)
            case Registration(_, false, _, _, _) => updateRegistration(registerRequest, x)
            case Registration(_, true, _, _, _) => Future.successful(IS_AGENT)
          }
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case _ => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def register(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    agentRepository.findByArn(registerAgentRequest.arn).flatMap {
      case agent :: maybeOtherAgents if postcodeValidOrNotNeeded(agent, registerAgentRequest.postcode) =>
        regRepository.findRegistrations(registerAgentRequest.groupId).flatMap {
          case Nil => addRegistration(registerAgentRequest)
          case Registration(_, true, _, registerAgentRequest.arn, _) :: Nil => Future.successful(ALREADY_REGISTERED)
          case Registration(_, true, _, _, _) :: Nil => Future.successful(RESPONSE_OK)
          case Registration(_, false, _, _, _) :: Nil => Future.successful(IS_NOT_AGENT)
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case _ => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def verification(groupId: String, regimeId: String): Future[VerificationResponse] = {
    regRepository.findRegistrations(groupId).map {
      case Nil => false
      case Registration(_, false, _, _, y) :: Nil => y.contains(regimeId)
      case Registration(_, true, _, _, _) :: Nil => true
      case x :: xs => false
    }.map(VerificationResponse.apply)
  }

  def prepopulation(groupId: String, regimeId: String): Future[List[Registration]] = {
    regRepository.findRegistrations(groupId)
  }

  private def addRegistration(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    regRepository.register(registerRequest).map {
      case Right(_) => RESPONSE_OK
      case Left(x) => RegistrationResponse(Some(x))
    }
  }

  private def addRegistration(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    regRepository.register(registerAgentRequest).map {
      case Right(_) => RESPONSE_OK
      case Left(x) => RegistrationResponse(Some(x))
    }
  }

  private def updateRegistration(registerRequest: RegisterRequest, registration: Registration): Future[RegistrationResponse] = {
    if (registration.regimeIds.contains(registerRequest.regimeId))
      Future.successful(RESPONSE_OK)
    else
      regRepository.addRegime(registration, registerRequest.regimeId).map {
        case Right(_) => RESPONSE_OK
        case Left(x) => RegistrationResponse(Some(x))
      }
  }
}

object RegistrationService extends RegistrationService {
  lazy val regRepository = registrationRepository
  lazy val userRepository = etmpBusinessUserRepository
  lazy val agentRepository = etmpAgentRepository
}
