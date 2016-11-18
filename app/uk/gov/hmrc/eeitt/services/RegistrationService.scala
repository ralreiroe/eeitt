package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.PostcodeValidator._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    new FindRegistration[RegisterBusinessUserRequest, IndividualRegistration] {
      def apply(req: RegisterBusinessUserRequest): Future[List[IndividualRegistration]] =
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

trait PostCode[A] {
  def apply(a: A): Option[String]
}

object PostCode {
  implicit val businessUserPostCode = new PostCode[RegisterBusinessUserRequest] {
    def apply(req: RegisterBusinessUserRequest): Option[String] = req.postcode
  }

  implicit val agentPostCode = new PostCode[RegisterAgentRequest] {
    def apply(req: RegisterAgentRequest): Option[String] = req.postcode
  }
}

trait FindUser[A, B] {
  def apply(a: A): Future[List[B]]
}

object FindUser {
  implicit def agentExists(implicit repository: EtmpAgentRepository) = {
    new FindUser[RegisterAgentRequest, EtmpAgent] {
      def apply(req: RegisterAgentRequest): Future[List[EtmpAgent]] =
        repository.findByArn(req.arn)
    }
  }

  implicit def indivitualExists(implicit repository: EtmpBusinessUsersRepository) = {
    new FindUser[RegisterBusinessUserRequest, EtmpBusinessUser] {
      def apply(req: RegisterBusinessUserRequest): Future[List[EtmpBusinessUser]] =
        repository.findByRegistrationNumber(req.registrationNumber)
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
    new AddRegistration[RegisterBusinessUserRequest] {
      def apply(req: RegisterBusinessUserRequest): Future[Either[String, Unit]] = repository.register(req)
    }
  }
}

trait RegistrationService {

  def register[A <: RegisterRequest, B, C: PostcodeValidator](
    registerRequest: A
  )(
    implicit
    findRegistration: FindRegistration[A, B],
    addRegistration: AddRegistration[A],
    findUser: FindUser[A, C],
    postCode: PostCode[A]
  ): Future[RegistrationResponse] = {
    findUser(registerRequest).flatMap {
      case user :: maybeOtherUsers if postcodeValidOrNotNeeded(user, postCode(registerRequest)) =>
        findRegistration(registerRequest).flatMap {
          case Nil => addRegistration(registerRequest).map {
            case Right(_) => RESPONSE_OK
            case Left(x) => RegistrationResponse(Some(x))
          }
          case x :: Nil => Future.successful(ALREADY_REGISTERED)
          case x :: xs => Future.successful(MULTIPLE_FOUND)
        }
      case _ =>
        registerRequest match {
          case x: RegisterAgentRequest => Future.successful(INCORRECT_KNOWN_FACTS_AGENTS)
          case x: RegisterRequest => Future.successful(INCORRECT_KNOWN_FACTS_BUSINESS_USERS)
        }
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
