package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentVerificationService {

  def enrolmentRepo: EnrolmentRepository

  def verify(enrolmentRequest: EnrolmentVerificationRequest): Future[EnrolmentVerificationResponse] = {
    enrolmentRepo.lookupEnrolment(enrolmentRequest.registrationNumber).map { enrolments =>
      enrolments match {
        case Nil => ResponseNotFound
        case x :: xs => doVerify(enrolmentRequest, x)
      }
    }
  }

  private def doVerify(enrolmentRequest: EnrolmentVerificationRequest, enrolmentFound: Enrolment): EnrolmentVerificationResponse = {
    if (enrolmentFound.formTypeRef != enrolmentRequest.formTypeRef) RegisteredForDifferentFormType
    else ResponseOk
  }
}

object EnrolmentVerificationService extends EnrolmentVerificationService {
  lazy val enrolmentRepo = EnrolmentRepository
}
