package uk.gov.hmrc.eeitt.services

import play.api.libs.json.{ Json, JsObject }
import uk.gov.hmrc.eeitt.model.RegistrationResponse._
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.PostcodeValidator._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Show[A] {
  def apply(a: A): String
}

object Show {
  implicit val groupIdShow = new Show[GroupId] {
    def apply(groupId: GroupId): String = s"GroupId(${groupId.value})"
  }

  implicit val groupIdAndRegimeIdShow = new Show[(GroupId, RegimeId)] {
    def apply(groupIdAndRegimeId: (GroupId, RegimeId)): String = {
      val (groupId, regimeId) = groupIdAndRegimeId
      s"(GroupId(${groupId.value}), RegimeId(${regimeId.value}))"
    }
  }
}

trait PrepopulationData[A] {
  def apply(a: A): JsObject
}

object PrepopulationData {
  implicit val prepopBusinessUser = new PrepopulationData[RegistrationBusinessUser] {
    def apply(businessUser: RegistrationBusinessUser): JsObject = Json.obj("registrationNumber" -> businessUser.registrationNumber.value)
  }

  implicit val prepopAgent = new PrepopulationData[RegistrationAgent] {
    def apply(agent: RegistrationAgent): JsObject = Json.obj("arn" -> agent.arn.value)
  }
}

trait FindRegistration[A] {
  type Out
  def apply(a: A): Future[List[Out]]
}

object FindRegistration {

  type Aux[A, Out0] = FindRegistration[A] { type Out = Out0 }

  implicit def businessUserByRequest(implicit repository: RegistrationRepository) = {
    new FindRegistration[RegisterBusinessUserRequest] {
      type Out = RegistrationBusinessUser
      def apply(req: RegisterBusinessUserRequest): Future[List[RegistrationBusinessUser]] =
        repository.findRegistrations(req.groupId, req.regimeId)
    }
  }

  implicit def businessUserByGroupIdAndRegimeId(implicit regRepository: RegistrationRepository) = {
    new FindRegistration[(GroupId, RegimeId)] {
      type Out = RegistrationBusinessUser
      def apply(groupIdAndRegimeId: (GroupId, RegimeId)): Future[List[RegistrationBusinessUser]] = {
        val (groupId, regimeId) = groupIdAndRegimeId
        regRepository.findRegistrations(groupId, regimeId)
      }
    }
  }

  implicit def agentByRequest(implicit repository: RegistrationAgentRepository) = {
    new FindRegistration[RegisterAgentRequest] {
      type Out = RegistrationAgent
      def apply(req: RegisterAgentRequest): Future[List[RegistrationAgent]] =
        repository.findRegistrations(req.groupId)
    }
  }

  implicit def agentByGroupId(implicit regRepository: RegistrationAgentRepository) = {
    new FindRegistration[GroupId] {
      type Out = RegistrationAgent
      def apply(groupId: GroupId): Future[List[RegistrationAgent]] =
        regRepository.findRegistrations(groupId)
    }
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

object RegistrationService {

  def register[A <: RegisterRequest, B: PostcodeValidator](
    registerRequest: A
  )(
    implicit
    findRegistration: FindRegistration[A],
    addRegistration: AddRegistration[A],
    findUser: FindUser[A, B],
    getPostCode: GetPostcode[RegisterRequest]
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

  def findRegistration[A](findParams: A)(
    implicit
    findRegistration: FindRegistration[A]
  ): Future[List[findRegistration.Out]] = {
    findRegistration(findParams)
  }
}
