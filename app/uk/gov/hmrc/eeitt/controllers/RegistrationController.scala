package uk.gov.hmrc.eeitt.controllers

import play.Logger
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc._
import uk.gov.hmrc.eeitt.model.{ AffinityGroup, Agent, RegisterRequest }
import uk.gov.hmrc.eeitt.model.RegisterAgentRequest
import uk.gov.hmrc.eeitt.services.RegistrationService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.eeitt.model.{ GroupId, RegimeId }

import scala.concurrent.Future

object RegistrationController extends RegistrationController {
  val registrationService = RegistrationService
}

trait RegistrationController extends BaseController {
  val registrationService: RegistrationService

  def verification(groupId: String, regimeId: String, affinityGroup: AffinityGroup) = Action.async { implicit request =>
    affinityGroup match {
      case Agent => registrationService.agentVerification(GroupId(groupId)) map (response => Ok(Json.toJson(response)))
      case _ => registrationService.individualVerification(GroupId(groupId), RegimeId(regimeId)) map (response => Ok(Json.toJson(response)))
    }
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

  def register() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterRequest] match {
      case JsSuccess(req, _) =>
        registrationService.register(req) map (response => Ok(Json.toJson(response)))
      case JsError(jsonErrors) =>
        Logger.debug(s"incorrect request: ${jsonErrors} ")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(jsonErrors))))
    }
  }

  def registerAgent() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegisterAgentRequest] match {
      case JsSuccess(req, _) =>
        registrationService.register(req) map (response => Ok(Json.toJson(response)))
      case JsError(jsonErrors) =>
        Logger.debug(s"incorrect request: ${jsonErrors} ")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(jsonErrors))))
    }
  }

}
