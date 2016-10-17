package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.{ JsError, JsSuccess, Json }
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.services.EnrolmentVerificationService

import scala.concurrent.Future

object EnrolmentController extends EnrolmentController {
  val enrolmentVerificationService = EnrolmentVerificationService
}

trait EnrolmentController extends BaseController {
  val enrolmentVerificationService: EnrolmentVerificationService

  def verify() = Action.async(parse.json) { implicit request =>
    request.body.validate[EnrolmentVerificationRequest] match {
      case JsSuccess(req, _) =>
        enrolmentVerificationService.verify(req) map (response => Ok(Json.toJson(response)))
      case JsError(errs) =>
        Future(BadRequest(Json.toJson(ResponseNotFound)))
    }
  }
}