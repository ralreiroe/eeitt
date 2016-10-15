package uk.gov.hmrc.eeitt.controllers

import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import scala.concurrent.Future

object MicroservicePing extends MicroservicePing

trait MicroservicePing extends BaseController {

  def ping() = Action.async { implicit request =>
    Future.successful(Ok("pong"))
  }
}