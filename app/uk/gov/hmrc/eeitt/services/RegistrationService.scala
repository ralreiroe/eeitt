package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Verification[A] {
  def apply(groupId: GroupId, regimeId: RegimeId): ((GroupId, RegimeId) => Future[List[A]]) => Future[VerificationResponse] =
    findRegistrations => findRegistrations(groupId, regimeId).map(_.size == 1).map(VerificationResponse.apply)
}

object Verification {
  implicit val AgentVerification = new Verification[AgentRegistration] {}

  implicit val IndividualVerification = new Verification[IndividualRegistration] {}
}

trait VerificationRepo[A] {
  def apply(groupId: GroupId, regimeId: RegimeId): Future[List[A]]
}

object VerificationRepo {
  implicit def agentRepo(implicit regRepository: AgentRegistrationRepository) = {
    new VerificationRepo[AgentRegistration] {
      def apply(groupId: GroupId, regimeId: RegimeId): Future[List[AgentRegistration]] = regRepository.findRegistrations(groupId)
    }
  }

  implicit def individualRepo(implicit regRepository: RegistrationRepository) = {
    new VerificationRepo[IndividualRegistration] {
      def apply(groupId: GroupId, regimeId: RegimeId): Future[List[IndividualRegistration]] = regRepository.findRegistrations(groupId, regimeId)
    }
  }
}

trait FindRegistration[A, B] {
  def apply(a: A): Future[List[B]]
}

object FindRegistration {
  implicit def individualRepo(implicit repository: RegistrationRepository) = {
    new FindRegistration[RegisterRequest, IndividualRegistration] {
      def apply(req: RegisterRequest): Future[List[IndividualRegistration]] =
        repository.findRegistrations(req.groupId, req.regimeId)
    }
  }

  implicit def agentRepo(implicit repository: AgentRegistrationRepository) = {
    new FindRegistration[RegisterAgentRequest, AgentRegistration] {
      def apply(req: RegisterAgentRequest): Future[List[AgentRegistration]] =
        repository.findRegistrations(req.groupId)
    }
  }
}

trait UserExists[A] {
  def apply(a: A): Future[Boolean]
}

object UserExists {
  implicit def agentExists(implicit repository: EtmpAgentRepository) = {
    new UserExists[RegisterAgentRequest] {
      def apply(req: RegisterAgentRequest): Future[Boolean] =
        repository.agentExists(EtmpAgent(req.arn))
    }
  }

  implicit def indivitualExists(implicit repository: EtmpBusinessUsersRepository) = {
    new UserExists[RegisterRequest] {
      def apply(req: RegisterRequest): Future[Boolean] =
        repository.userExists(EtmpBusinessUser(req.registrationNumber, req.postcode))
    }
  }
}

trait AddRegistration[A] {
  def apply(a: A): Future[Either[String, Unit]]
}

object AddRegistration {
  implicit def agentRepo(implicit repository: AgentRegistrationRepository) = {
    new AddRegistration[RegisterAgentRequest] {
      def apply(req: RegisterAgentRequest): Future[Either[String, Unit]] = repository.register(req)
    }
  }

  implicit def individualRepo(implicit repository: RegistrationRepository) = {
    new AddRegistration[RegisterRequest] {
      def apply(req: RegisterRequest): Future[Either[String, Unit]] = repository.register(req)
    }
  }
}

trait RegistrationService {

  def register[A, B](
    registerRequest: A
  )(
    implicit
    findRegistration: FindRegistration[A, B],
    addRegistration: AddRegistration[A],
    userExists: UserExists[A]
  ): Future[RegistrationResponse] = {
    userExists(registerRequest).flatMap {
      case true =>
        findRegistration(registerRequest).flatMap {
          case Nil => addRegistration(registerRequest).map {
            case Right(_) => RESPONSE_OK
            case Left(x) => RegistrationResponse(Some(x))
          }
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
    verification(groupId, regimeId)(vr.apply)
  }

  def prepopulation(groupId: String, regimeId: String): Future[List[AgentRegistration]] = {
    Future.successful(List.empty[AgentRegistration])
  }
}

object RegistrationService extends RegistrationService
