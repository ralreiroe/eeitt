package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentRepositorySupport extends UnitSpec with MongoSpecSupport {
  val enroRepo = new MongoEnrolmentRepository

  def insertEnrolment(enrolment: Enrolment): Unit = {
    val e = Enrolment(enrolment.formTypeRef, enrolment.registrationNumber, enrolment.livesInTheUk, enrolment.postcode, enrolment.arn)
    await(enroRepo.collection.insert(e))
  }

}
