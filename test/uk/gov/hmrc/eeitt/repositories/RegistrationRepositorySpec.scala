package uk.gov.hmrc.eeitt.repositories

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.specs2.matcher.ExceptionMatchers
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.eeitt.model.Registration
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationRepositorySpec extends UnitSpec with ExceptionMatchers with RegistrationRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  private val registration1: Registration = Registration("g1", List("LT", "LL"), "12LT001", "SE39EP")
  private val registration2: Registration = Registration("g2", List("LT", "LL", "XL"), "12LT002", "SE39EX")

  "query registrations with a group id" should {
    "produce registration with the given group id" in {
      insertRegistration(registration1)
      insertRegistration(registration2)
      await(repo.count) shouldBe 2
      await(repo.findRegistrations("g1")) map (_.groupId) should contain theSameElementsAs (List("g1"))
    }
  }

  "inserting more than one registration with the same group id" should {
    "result in an error" in {
      insertRegistration(registration1)
      intercept[DatabaseException](insertRegistration(registration1))
    }
  }

  "check registrations with a group id and a form id" should {
    "produce a registration with the given id" in {
      insertRegistration(Registration("g1", List("LT", "LL"), "12LT001", "SE39EP"))
      insertRegistration(Registration("g2", List("LT", "LL", "XL"), "12LT002", "SE39ER"))
      await(repo.count) shouldBe 2
      await(repo.check("g2", "XL")) map (_.groupId) should contain theSameElementsAs (List("g2"))
      await(repo.check("g2", "LL")) map (_.groupId) should contain theSameElementsAs (List("g2"))
      await(repo.check("g2", "LT")) map (_.groupId) should contain theSameElementsAs (List("g2"))
      await(repo.check("g2", "XX")).size shouldBe 0
      await(repo.check("g1", "XL")).size shouldBe 0
    }
  }

}
