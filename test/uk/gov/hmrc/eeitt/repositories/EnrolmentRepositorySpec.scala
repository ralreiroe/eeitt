package uk.gov.hmrc.eeitt.repositories

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  val repo = new EnrolmentRepository
  val fakeId = BSONObjectID.generate

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "query all enrolments" should {

    "produce all enrolments from repository" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT31", "SE39EP"))
      insertEnrolment(Enrolment(fakeId, "2", "12LT32", "SE39XY"))
      insertEnrolment(Enrolment(fakeId, "3", "12LT33", "SE39XZ"))
      repo.count.futureValue shouldBe 3

      await(repo.getAllEnrolments().size) shouldBe 3
    }

  }

  "query all enrolments for form id" should {

    "produce enrolments from repository" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT34", "SE39EP"))
      insertEnrolment(Enrolment(fakeId, "1", "12LT35", "SE39XY"))
      repo.count.futureValue shouldBe 2

      await(repo.getAllEnrolmentsWithFormId("1").size) shouldBe 2
    }

  }

  def insertEnrolment(enrolment: Enrolment): BSONObjectID = {
    val lease = Enrolment(id = BSONObjectID.generate, enrolment.formId, enrolment.registrationId, enrolment.postalCode)
    await(repo.collection.insert(lease))
    lease.id
  }

}
