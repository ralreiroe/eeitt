package uk.gov.hmrc.eeitt

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.eeitt.repositories.EnrolmentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySupport extends UnitSpec with MongoSpecSupport {
  val repo = new EnrolmentRepository
  val fakeId = BSONObjectID.generate

  def insertEnrolment(enrolment: Enrolment): BSONObjectID = {
    val e = Enrolment(_id = BSONObjectID.generate, enrolment.formTypeRef, enrolment.registrationNumber, enrolment.livesInTheUk, enrolment.postcode)
    await(repo.collection.insert(e))
    e._id
  }

}
