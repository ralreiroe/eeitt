package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import uk.gov.hmrc.eeitt.services.EnrolmentStoreService

import scala.concurrent.Future

object EnrolmentController extends EnrolmentController

trait EnrolmentController extends BaseController {

  val enrolmentStoreService = EnrolmentStoreService

  def enrolments() = Action.async { implicit request =>
    enrolmentStoreService.getEnrolments().map {
      enrolmentList => Ok(Json.toJson(enrolmentList))
    }
  }

}