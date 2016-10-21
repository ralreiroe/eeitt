package uk.gov.hmrc.eeitt.repositories

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

trait RepositorySupport extends UnitSpec with MongoSpecSupport {
  val repo = new MongoEnrolmentRepository
  val fakeId = BSONObjectID.generate

  def insertEnrolment(enrolment: Enrolment): BSONObjectID = {
    val e = Enrolment(_id = BSONObjectID.generate, enrolment.formTypeRef, enrolment.registrationNumber, enrolment.livesInTheUk, enrolment.postcode, enrolment.arn)
    await(repo.collection.insert(e))
    e._id
  }

}
