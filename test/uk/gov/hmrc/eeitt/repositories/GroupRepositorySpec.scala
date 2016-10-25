package uk.gov.hmrc.eeitt.repositories

import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.specs2.matcher.ExceptionMatchers
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.eeitt.model.Group
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class GroupRepositorySpec extends UnitSpec with ExceptionMatchers with GroupRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  "query groups with a group id" should {
    "produce all groups with the id" in {
      insertGroup(Group("g1", List("LT", "LL")))
      insertGroup(Group("g2", List("LT", "LL", "XL")))
      await(repo.count) shouldBe 2
      await(repo.lookupGroup("g1")).size shouldBe 1
    }
  }

  "inserting more than one groups with the same group id" should {
    "result in an error" in {
      insertGroup(Group("g1", List("LT", "LL")))
      intercept[DatabaseException](insertGroup(Group("g1", List("LT", "LL", "XL"))))
    }
  }

}
