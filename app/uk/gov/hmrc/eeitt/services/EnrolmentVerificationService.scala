package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentVerificationService {

  def enrolmentRepo: EnrolmentRepository

  def verify(enrolmentRequest: EnrolmentVerificationRequest): Future[EnrolmentResponse] = {
    enrolmentRepo.lookupEnrolment(enrolmentRequest.registrationNumber).map { enrolments =>
      enrolments match {
        case Nil => EnrolmentResponseNotFound
        case x :: Nil => doVerify(enrolmentRequest, x)
        case _ => LookupProblem
      }
    }
  }

  private def doVerify(enrolmentRequest: EnrolmentVerificationRequest, enrolmentFound: Enrolment): EnrolmentResponse = {
    if (enrolmentFound.formTypeRef != enrolmentRequest.formTypeRef) RegisteredForDifferentFormType
    else EnrolmentResponseOk
  }
}

object EnrolmentVerificationService extends EnrolmentVerificationService {
  lazy val enrolmentRepo = EnrolmentRepository
}
