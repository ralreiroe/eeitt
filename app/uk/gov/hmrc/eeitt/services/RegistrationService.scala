package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Verification[O] {

  def validRegistration(registrations: List[O]): Boolean

  def verification(groupId: GroupId, regimeId: RegimeId): ((GroupId, RegimeId) => Future[List[O]]) => Future[VerificationResponse] =
    findRegistrations => findRegistrations(groupId, regimeId).map(validRegistration).map(VerificationResponse.apply)
}

trait VerificationRepo[O] {
  def verRepo(groupId: GroupId, regimeId: RegimeId): Future[List[O]]
}

package object implicits {
  implicit val AgentVerification = new Verification[AgentRegistration] {
    def validRegistration(registrations: List[AgentRegistration]): Boolean =
      registrations match {
        case Nil => false
        case AgentRegistration(_, _) :: Nil => true
        case x :: xs => false
      }
  }

  implicit val IndividualVerification = new Verification[IndividualRegistration] {
    def validRegistration(registrations: List[IndividualRegistration]): Boolean =
      registrations match {
        case Nil => false
        case IndividualRegistration(_, _, _) :: Nil => true
        case x :: xs => false
      }
  }

  implicit def agentRepo(implicit regRepository: AgentRegistrationRepository) = {
    new VerificationRepo[AgentRegistration] {
      def verRepo(groupId: GroupId, regimeId: RegimeId): Future[List[AgentRegistration]] = regRepository.findRegistrations(groupId)
    }
  }

  implicit def individualRepo(implicit regRepository: RegistrationRepository) = {
    new VerificationRepo[IndividualRegistration] {
      def verRepo(groupId: GroupId, regimeId: RegimeId): Future[List[IndividualRegistration]] = regRepository.findRegistrations(groupId, regimeId)
    }
  }
}

trait RegistrationService {

  def regRepository: RegistrationRepository

  def agentRegRepository: AgentRegistrationRepository

  def userRepository: EtmpBusinessUsersRepository

  def agentRepository: EtmpAgentRepository

  def register(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    userRepository.userExists(EtmpBusinessUser(registerRequest.registrationNumber, registerRequest.postcode)).flatMap {
      case true =>
        regRepository.findRegistrations(registerRequest.groupId, registerRequest.regimeId).flatMap {
          case Nil => addRegistration(registerRequest)
          case x :: Nil => Future.successful(ALREADY_REGISTERED)
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case false => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def register(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    agentRepository.agentExists(EtmpAgent(registerAgentRequest.arn)).flatMap {
      case true =>
        agentRegRepository.findRegistrations(registerAgentRequest.groupId).flatMap {
          case Nil => agentAddRegistration(registerAgentRequest)
          case x :: Nil => Future.successful(ALREADY_REGISTERED)
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case false => Future.successful(INCORRECT_KNOWN_FACTS)
    }
  }

  def verify[A](
    groupId: GroupId,
    regimeId: RegimeId
  )(
    implicit
    verification: Verification[A],
    vr: VerificationRepo[A]
  ): Future[VerificationResponse] = {
    verification.verification(groupId, regimeId)(vr.verRepo)
  }

  def prepopulation(groupId: String, regimeId: String): Future[List[AgentRegistration]] = {
    ??? //regRepository.findRegistrations(groupId)
  }

  private def addRegistration(registerRequest: RegisterRequest): Future[RegistrationResponse] = {
    regRepository.register(registerRequest).map {
      case Right(_) => RESPONSE_OK
      case Left(x) => RegistrationResponse(Some(x))
    }
  }

  private def agentAddRegistration(registerAgentRequest: RegisterAgentRequest): Future[RegistrationResponse] = {
    agentRegRepository.register(registerAgentRequest).map {
      case Right(_) => RESPONSE_OK
      case Left(x) => RegistrationResponse(Some(x))
    }
  }
}

object RegistrationService extends RegistrationService {
  lazy val regRepository = registrationRepository
  lazy val agentRegRepository = agentRegistrationRepository
  lazy val userRepository = etmpBusinessUserRepository
  lazy val agentRepository = etmpAgentRepository
}
