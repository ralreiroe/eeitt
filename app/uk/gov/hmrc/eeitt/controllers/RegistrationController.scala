package uk.gov.hmrc.eeitt.controllers

import play.Logger
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.mvc._
import uk.gov.hmrc.eeitt.model.RegistrationRequest
import uk.gov.hmrc.eeitt.services.RegistrationService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

object RegistrationController extends RegistrationController {
  val registrationService = RegistrationService
}

trait RegistrationController extends BaseController {
  val registrationService: RegistrationService

  def regimes(groupId: String) = Action.async { implicit request =>
    registrationService.lookup(groupId) map (response => Ok(Json.toJson(response)))
  }

  def register() = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationRequest] match {
      case JsSuccess(req, _) =>
        registrationService.register(req) map (response => Ok(Json.toJson(response)))
      case JsError(jsonErrors) =>
        Logger.debug(s"incorrect request: ${jsonErrors} ")
        Future.successful(BadRequest(Json.obj("message" -> JsError.toFlatJson(jsonErrors))))
    }
  }
}
