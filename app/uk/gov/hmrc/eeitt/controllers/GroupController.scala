package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.eeitt.services.GroupLookupService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.microservice.controller.BaseController

object GroupController extends GroupController {
  val groupLookupService = GroupLookupService
}

trait GroupController extends BaseController {
  val groupLookupService: GroupLookupService

  def regimes(groupId: String) = Action.async { implicit request =>
    groupLookupService.lookup(groupId) map (response => Ok(Json.toJson(response)))
  }
}
