package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ RESPONSE_OK, RESPONSE_NOT_FOUND, INCORRECT_REGIME, INCORRECT_POSTCODE }
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

  private def doVerify(request: EnrolmentVerificationRequest, enrolment: Enrolment): EnrolmentVerificationResponse = {
    EnrolmentVerificationResponse((request, enrolment) match {
      case DifferentPostcodes() => INCORRECT_POSTCODE
      case DifferentFormTypes() => INCORRECT_REGIME
      case DifferentArns() => INCORRECT_REGIME
      case _ => RESPONSE_OK
    })
  }

  object DifferentPostcodes {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => normalize(r.postcode) != normalize(e.postcode)
    }
    private def normalize(p: String) = p.trim.toUpperCase.replaceAll("\\s", "")
  }

  object DifferentFormTypes {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => r.formTypeRef != e.formTypeRef
    }
  }

  object DifferentArns {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => r.maybeArn != e.maybeArn
    }
  }

}

object EnrolmentVerificationService extends EnrolmentVerificationService {
  lazy val enrolmentRepo = EnrolmentRepository
}

