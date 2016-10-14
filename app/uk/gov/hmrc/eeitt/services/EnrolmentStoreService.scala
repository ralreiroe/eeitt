package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.repositories.{ Enrolment, EnrolmentRepository }

import scala.concurrent.Future

trait EnrolmentStoreService {

  def enrolmentRepo: EnrolmentRepository

  def getEnrolments(): Future[List[Enrolment]] = {
    enrolmentRepo.getAllEnrolments()
  }

}

object EnrolmentStoreService extends EnrolmentStoreService {
  lazy val enrolmentRepo = EnrolmentRepository
}
