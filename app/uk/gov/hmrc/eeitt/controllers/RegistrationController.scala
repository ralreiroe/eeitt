package uk.gov.hmrc.eeitt.controllers

import play.Logger
import play.api.libs.json.{ Format, JsError, JsSuccess, Json, Reads }
import play.api.mvc._
import uk.gov.hmrc.eeitt.model.{ AffinityGroup, Agent, RegistrationAgent, EtmpAgent, EtmpBusinessUser, RegistrationBusinessUser, RegisterBusinessUserRequest }
import uk.gov.hmrc.eeitt.model.RegisterAgentRequest
import uk.gov.hmrc.eeitt.services.PostcodeValidator
import uk.gov.hmrc.eeitt.services.{ RegistrationService, FindUser, FindRegistration, AddRegistration, GetPostcode }
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.eeitt.model.{ GroupId, RegimeId, RegisterRequest }
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.Verification
import uk.gov.hmrc.eeitt.services.VerificationRepo

import scala.concurrent.Future

object RegistrationController extends RegistrationController {
  val registrationService = RegistrationService
}

trait RegistrationController extends BaseController {
  val registrationService: RegistrationService

  implicit lazy val registrationRepo = registrationRepository
  implicit lazy val agentRegistrationRepo = agentRegistrationRepository
  implicit lazy val etmpBusinessUserRepo = etmpBusinessUserRepository
  implicit lazy val etmpAgentRepo = etmpAgentRepository

  def verification(groupId: String, regimeId: String, affinityGroup: AffinityGroup) = Action.async { request =>
    val gId = GroupId(groupId)
    val rId = RegimeId(regimeId)
    affinityGroup match {
      case Agent => verify[RegistrationAgent](gId, rId, request)
      case _ => verify[RegistrationBusinessUser](gId, rId, request)
    }
  }

  private def verify[A: Verification: VerificationRepo](groupId: GroupId, regimeId: RegimeId, request: Request[AnyContent]) = {

    implicit val r = request // we need to get ExecutionContext from request by MdcLoggingExecutionContext means

    registrationService.verify(groupId, regimeId) map (response => Ok(Json.toJson(response)))
  }

  def prepopulation(groupId: String, regimeId: String, affinityGroup: AffinityGroup) = Action.async { implicit request =>
    registrationService.prepopulation(groupId, regimeId).map {
      case registration :: Nil =>
        NotFound
      // Ok(Json.toJson(registration))
      case Nil =>
        Logger.warn(s"Prepopulation data not found for groupId: $groupId and regimeId: $regimeId")
        NotFound
      case registration :: xs =>
        Logger.warn(s"More than one prepopulation data found for groupId: $groupId and regimeId: $regimeId")
        NotFound
      // Ok(Json.toJson(registration))
    }
  }

  def registerBusinessUser() = register[RegisterBusinessUserRequest, RegistrationBusinessUser, EtmpBusinessUser]

  def registerAgent() = register[RegisterAgentRequest, RegistrationAgent, EtmpAgent]

  private def register[A <: RegisterRequest: Reads: AddRegistration: GetPostcode, B, C: PostcodeValidator](
    implicit
    findRegistration: FindRegistration[A, B],
    findUser: FindUser[A, C]
  ) = Action.async(parse.json) { implicit request =>
    request.body.validate match {
      case JsSuccess(req, _) =>
        registrationService.register(req).map(response => Ok(Json.toJson(response)))
      case JsError(jsonErrors) =>
        Logger.debug(s"incorrect request: ${jsonErrors} ")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(jsonErrors))))
    }
  }
}
