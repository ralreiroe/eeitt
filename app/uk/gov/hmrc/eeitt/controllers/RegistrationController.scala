package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.eeitt.services.RegistrationLookupService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

object RegistrationController extends RegistrationController {
  val registrationLookupService = RegistrationLookupService
}

trait RegistrationController extends BaseController {
  val registrationLookupService: RegistrationLookupService

  def regimes(groupId: String) = Action.async { implicit request =>
    registrationLookupService.lookup(groupId) map (response => Ok(Json.toJson(response)))
  }
}
