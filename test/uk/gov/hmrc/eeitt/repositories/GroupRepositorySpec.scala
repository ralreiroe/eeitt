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

  private val group1: Group = Group("g1", List("LT", "LL"), "12LT001", "SE39EP")
  private val group2: Group = Group("g2", List("LT", "LL", "XL"), "12LT002", "SE39EX")

  "query groups with a group id" should {
    "produce a group with the id" in {
      insertGroup(group1)
      insertGroup(group2)
      await(repo.count) shouldBe 2
      await(repo.lookupGroup("g1")).size shouldBe 1
    }
  }

  "inserting more than one groups with the same group id" should {
    "result in an error" in {
      insertGroup(group1)
      intercept[DatabaseException](insertGroup(group1))
    }
  }

  "check registrations with a group id and form id" should {
    "produce a group with the id" in {
      insertGroup(Group("g1", List("LT", "LL"), "12LT001", "SE39EP"))
      insertGroup(Group("g2", List("LT", "LL", "XL"), "12LT002", "SE39ER"))
      await(repo.count) shouldBe 2
      await(repo.check("g2", "XL")).size shouldBe 1
      await(repo.check("g2", "LL")).size shouldBe 1
      await(repo.check("g2", "LT")).size shouldBe 1
      await(repo.check("g2", "XX")).size shouldBe 0
      await(repo.check("g1", "XL")).size shouldBe 0
    }
  }

}
