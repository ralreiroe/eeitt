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
  implicit val AgentVerification = new Verification[RegistrationAgent] {}

  implicit val BusinessUserVerification = new Verification[RegistrationBusinessUser] {}
}

trait VerificationRepo[A] {
  def apply(groupId: GroupId, regimeId: RegimeId): Future[List[A]]
}

object VerificationRepo {
  implicit def agentRepo(implicit regRepository: RegistrationAgentRepository) = {
    new VerificationRepo[RegistrationAgent] {
      def apply(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationAgent]] =
        regRepository.findRegistrations(groupId) // we are ignoring RegimeId
    }
  }

  implicit def businessUserRepo(implicit regRepository: RegistrationRepository) = {
    new VerificationRepo[RegistrationBusinessUser] {
      def apply(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationBusinessUser]] =
        regRepository.findRegistrations(groupId, regimeId)
    }
  }
}

trait FindRegistration[A, B] {
  def apply(a: A): Future[List[B]]
}

object FindRegistration {
  implicit def businessUserRepo(implicit repository: RegistrationRepository) = {
    new FindRegistration[RegisterBusinessUserRequest, RegistrationBusinessUser] {
      def apply(req: RegisterBusinessUserRequest): Future[List[RegistrationBusinessUser]] =
        repository.findRegistrations(req.groupId, req.regimeId)
    }
  }

  implicit def agentRepo(implicit repository: RegistrationAgentRepository) = {
    new FindRegistration[RegisterAgentRequest, RegistrationAgent] {
      def apply(req: RegisterAgentRequest): Future[List[RegistrationAgent]] =
        repository.findRegistrations(req.groupId)
    }
  }
}

trait GetPostcode[A] {
  def apply(a: A): Option[Postcode]
}

object GetPostcode {
  implicit val businessUserGetPostcode = new GetPostcode[RegisterBusinessUserRequest] {
    def apply(req: RegisterBusinessUserRequest): Option[Postcode] = req.postcode
  }

  implicit val agentGetPostcode = new GetPostcode[RegisterAgentRequest] {
    def apply(req: RegisterAgentRequest): Option[Postcode] = req.postcode
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
  implicit def agentRepo(implicit repository: RegistrationAgentRepository) = {
    new AddRegistration[RegisterAgentRequest] {
      def apply(req: RegisterAgentRequest): Future[Either[String, Unit]] = repository.register(req)
    }
  }

  implicit def businessUserRepo(implicit repository: RegistrationRepository) = {
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
    getPostCode: GetPostcode[A]
  ): Future[RegistrationResponse] = {
    findUser(registerRequest).flatMap {
      case user :: maybeOtherUsers if postcodeValidOrNotNeeded(user, getPostCode(registerRequest)) =>
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
          case _: RegisterAgentRequest => Future.successful(INCORRECT_KNOWN_FACTS_AGENTS)
          case _: RegisterRequest => Future.successful(INCORRECT_KNOWN_FACTS_BUSINESS_USERS)
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

  def prepopulation(groupId: String, regimeId: String): Future[List[RegistrationAgent]] = {
    Future.successful(List.empty[RegistrationAgent])
  }
}

object RegistrationService extends RegistrationService
