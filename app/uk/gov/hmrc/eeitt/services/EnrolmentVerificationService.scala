package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.EnrolmentVerificationResponse.{ RESPONSE_OK, RESPONSE_NOT_FOUND, INCORRECT_REGIME, INCORRECT_POSTCODE, INCORRECT_ARN, MISSING_ARN, INCORRECT_ARN_FOR_CLIENT }
import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentVerificationService {

  def enrolmentRepo: EnrolmentRepository

  def verify(enrolmentRequest: EnrolmentVerificationRequest): Future[EnrolmentVerificationResponse] = {
    enrolmentRepo.lookupEnrolment(enrolmentRequest.registrationNumber).flatMap { enrolments =>
      enrolments match {
        case Nil => Future.successful(EnrolmentVerificationResponse(RESPONSE_NOT_FOUND))
        case x :: xs => doVerify(enrolmentRequest, x)
      }
    }
  }

  private def doVerify(request: EnrolmentVerificationRequest, enrolment: Enrolment): Future[EnrolmentVerificationResponse] = {
    (request, enrolment) match {
      case DifferentPostcodes() => Future.successful(EnrolmentVerificationResponse(INCORRECT_POSTCODE))
      case DifferentFormTypes() => Future.successful(EnrolmentVerificationResponse(INCORRECT_REGIME))
      case DifferentArns() if request.isAgent => doVerifyArn(request)
      case _ => Future.successful(EnrolmentVerificationResponse(RESPONSE_OK))
    }
  }

  private def doVerifyArn(request: EnrolmentVerificationRequest): Future[EnrolmentVerificationResponse] = {
    request.arn match {
      case _ if request.arn.isEmpty => Future.successful(EnrolmentVerificationResponse(MISSING_ARN))
      case arn =>
        enrolmentRepo.getEnrolmentsWithArn(arn).map { enrolments =>
          enrolments match {
            case Nil => EnrolmentVerificationResponse(INCORRECT_ARN)
            case x :: xs => EnrolmentVerificationResponse(INCORRECT_ARN_FOR_CLIENT)
          }
        }
    }
  }

  object DifferentPostcodes {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => (e.livesInTheUk && (norm(r.postcode) != norm(e.postcode))) || (r.livesInTheUk != e.livesInTheUk)
    }
    private def norm(p: String) = p.trim.toUpperCase.replaceAll("\\s", "")
  }

  object DifferentFormTypes {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => r.formTypeRef != e.formTypeRef
    }
  }

  object DifferentArns {
    def unapply(p: (EnrolmentVerificationRequest, Enrolment)): Boolean = p match {
      case (r, e) => r.arn != e.arn
    }
  }

}

object EnrolmentVerificationService extends EnrolmentVerificationService {
  lazy val enrolmentRepo = EnrolmentRepository
}

