package uk.gov.hmrc.eeitt.repositories

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import uk.gov.hmrc.eeitt.model.Enrolment
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentRepositorySpec extends UnitSpec with EnrolmentRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "query enrolments with ARN" should {
    "produce all enrolments from repository with a given ARN" in {
      insertEnrolment(Enrolment("1", "12LT34", true, "SE39EP", "555555555555555"))
      insertEnrolment(Enrolment("1", "12LT35", true, "SE39XY", "555555555555555"))
      await(repo.count) shouldBe 2
      await(repo.getEnrolmentsWithArn("555555555555555")).size shouldBe 2
    }
  }

  "lookup enrolments by registration number " should {
    "find enrolment with a given registration number" in {
      insertEnrolment(Enrolment("1", "12LT34", true, "SE39EP", ""))
      insertEnrolment(Enrolment("1", "12LT35", true, "SE39XY", ""))
      await(repo.count) shouldBe 2
      await(repo.lookupEnrolment("12LT35")).size shouldBe 1
    }
  }

}
