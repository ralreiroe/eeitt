package uk.gov.hmrc.eeitt.controllers

import play.Logger
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc._
import uk.gov.hmrc.eeitt.model.RegisterRequest
import uk.gov.hmrc.eeitt.model.RegisterAgentRequest
import uk.gov.hmrc.eeitt.services.{ EnrolmentVerificationService, RegistrationService }
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

object RegistrationController extends RegistrationController {
  val registrationService = RegistrationService
  val enrolmentVerificationService = EnrolmentVerificationService
}

trait RegistrationController extends BaseController {
  val registrationService: RegistrationService

  def verification(groupId: String, regimeId: String) = Action.async { implicit request =>
    registrationService.verification(groupId, regimeId) map (response => Ok(Json.toJson(response)))
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
        registrationService.registerAgent(req) map (response => Ok(Json.toJson(response)))
      case JsError(jsonErrors) =>
        Logger.debug(s"incorrect request: ${jsonErrors} ")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(jsonErrors))))
    }
  }

}
