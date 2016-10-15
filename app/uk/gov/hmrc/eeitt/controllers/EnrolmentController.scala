package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.{ JsError, JsSuccess, Json }
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.services.EnrolmentVerificationService

import scala.concurrent.Future

object EnrolmentController extends EnrolmentController {
  val enrolmentStoreService = EnrolmentVerificationService
}

trait EnrolmentController extends BaseController {

  this: BaseController =>

  val enrolmentStoreService: EnrolmentVerificationService

  def verify() = Action.async(parse.json) { implicit request =>
    request.body.validate[EnrolmentVerificationRequest] match {
      case JsSuccess(req, _) =>
        enrolmentStoreService.verify(req) map (response => Ok(Json.toJson(response)))
      case JsError(errs) =>
        Future(BadRequest(Json.toJson(IncorrectRequest)))
    }
  }

  //  def check = Action.async { implicit request =>
  //
  //     extract parameters from request
  //    val formId = request.getQueryString("formId").getOrElse("")
  //    val registrationNo = request.getQueryString("registrationNo").getOrElse("")
  //    val postCode = request.getQueryString("postCode").getOrElse("")
  //
  //     create enrolled
  //    val enrolled = new Enrolment(BSONObjectID.generate, formId, registrationNo, postCode)
  //
  //     determine enrolled status
  //    val isEnrolled = enrolled.is
  //    val isRegistrationNumberOk = enrolled.isRegistrationNumberOk
  //    val isValidPostcode = enrolled.isValidPostCode
  //
  //     debug
  //    println("IS_ENROLLED = " + isEnrolled)
  //    println("REGISTRATION_OK = " + isRegistrationNumberOk)
  //    println("VALID_POSTCODE = " + isValidPostcode)
  //
  //
  //     return result to page
  //        Future.successful(Ok(enrolment.render(isEnrolled.toString, isRegistrationNumberOk.toString, isValidPostcode.toString, Enrolled.table)))
  //
  //  }

}