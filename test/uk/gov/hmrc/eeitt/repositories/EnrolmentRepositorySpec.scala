package uk.gov.hmrc.eeitt.repositories

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import uk.gov.hmrc.eeitt.RepositorySupport
import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentRepositorySpec extends UnitSpec with RepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "query all enrolments" should {
    "produce all enrolments from repository" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT31", true, "SE39EP"))
      insertEnrolment(Enrolment(fakeId, "2", "12LT32", true, "SE39XY"))
      insertEnrolment(Enrolment(fakeId, "3", "12LT33", true, "SE39XZ"))
      await(repo.count) shouldBe 3
      await(repo.getEnrolments()).size shouldBe 3
    }
  }

  "query enrolments with a form type" should {
    "produce all enrolments from repository with a given form type" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT34", true, "SE39EP"))
      insertEnrolment(Enrolment(fakeId, "1", "12LT35", true, "SE39XY"))
      await(repo.count) shouldBe 2
      await(repo.getEnrolmentsWithFormType("1")).size shouldBe 2
    }
  }

  "query enrolments with ARN" should {
    "produce all enrolments from repository with a given ARN" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT34", true, "SE39EP", Some("555555555555555")))
      insertEnrolment(Enrolment(fakeId, "1", "12LT35", true, "SE39XY", Some("555555555555555")))
      await(repo.count) shouldBe 2
      await(repo.getEnrolmentsWithArn(Some("555555555555555"))).size shouldBe 2
    }
    "produce all enrolments from repository without ARN" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT35", true, "SE39XY"))
      insertEnrolment(Enrolment(fakeId, "1", "12LT36", true, "SE39ZZ"))
      await(repo.count) shouldBe 2
      await(repo.getEnrolmentsWithArn(None)).size shouldBe 2
    }
  }

  "lookup enrolments by registration number " should {
    "find enrolment with a given registration number" in {
      insertEnrolment(Enrolment(fakeId, "1", "12LT34", true, "SE39EP"))
      insertEnrolment(Enrolment(fakeId, "1", "12LT35", true, "SE39XY"))
      await(repo.count) shouldBe 2
      await(repo.lookupEnrolment("12LT35")).size shouldBe 1
    }
  }

}
