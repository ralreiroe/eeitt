package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ Enrolment, EnrolmentResponse }
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository

import scala.concurrent.Future

trait EnrolmentStoreService {

  def enrolmentRepo: EnrolmentRepository

  def getEnrolments(): Future[List[Enrolment]] = {
    enrolmentRepo.getAllEnrolments()
  }

  def lookupEnrolment(enrolment: Enrolment): Future[EnrolmentResponse] = ???

}

object EnrolmentStoreService extends EnrolmentStoreService {
  lazy val enrolmentRepo = EnrolmentRepository
}
