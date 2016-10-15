package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model._
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentStoreService {

  def enrolmentRepo: EnrolmentRepository

  def getEnrolments(): Future[List[Enrolment]] = {
    enrolmentRepo.getAllEnrolments()
  }

  def lookupEnrolment(enrolment: EnrolmentVerificationRequest): Future[EnrolmentResponse] = {
    enrolmentRepo.lookupEnrolment(enrolment.registrationNumber).map { enrolments =>
      enrolments match {
        case Nil => EnrolmentResponseNotFound
        case x :: Nil => EnrolmentResponseOk
        case xs if xs.size > 1 => MultipleFound
        case _ => LookupProblem
      }
    }
  }
}

object EnrolmentStoreService extends EnrolmentStoreService {
  lazy val enrolmentRepo = EnrolmentRepository
}
