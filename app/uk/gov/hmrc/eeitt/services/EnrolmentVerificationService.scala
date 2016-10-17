package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ RESPONSE_OK, RESPONSE_NOT_FOUND, RESPONSE_DIFFERENT_FORM_TYPE }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentVerificationService {

  def enrolmentRepo: EnrolmentRepository

  def verify(enrolmentRequest: EnrolmentVerificationRequest): Future[EnrolmentVerificationResponse] = {
    enrolmentRepo.lookupEnrolment(enrolmentRequest.registrationNumber).map { enrolments =>
      enrolments match {
        case Nil => EnrolmentVerificationResponse(RESPONSE_NOT_FOUND)
        case x :: xs => doVerify(enrolmentRequest, x)
      }
    }
  }

  private def doVerify(enrolmentRequest: EnrolmentVerificationRequest, enrolmentFound: Enrolment): EnrolmentVerificationResponse = {
    enrolmentFound.formTypeRef match {
      case enrolmentRequest.formTypeRef => EnrolmentVerificationResponse(RESPONSE_OK)
      case _ => EnrolmentVerificationResponse(RESPONSE_DIFFERENT_FORM_TYPE)
    }
  }
}

object EnrolmentVerificationService extends EnrolmentVerificationService {
  lazy val enrolmentRepo = EnrolmentRepository
}
