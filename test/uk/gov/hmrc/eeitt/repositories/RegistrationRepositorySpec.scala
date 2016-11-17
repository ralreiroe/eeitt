package uk.gov.hmrc.eeitt.repositories

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.{ BeforeAndAfterEach, Inspectors, LoneElement }
import org.specs2.matcher.ExceptionMatchers
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.eeitt.model.{ GroupId, IndividualRegistration, RegimeId, RegistrationNumber }
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationRepositorySpec extends UnitSpec with ExceptionMatchers with RegistrationRepositorySupport with BeforeAndAfterEach with ScalaFutures with LoneElement with Inspectors with IntegrationPatience {

  override protected def beforeEach(): Unit = {
    await(regRepo.removeAll())
    awaitRegistrationIndexCreation()
  }

  private val registration1: IndividualRegistration = IndividualRegistration(GroupId("g1"), RegistrationNumber("12LT001"), RegimeId("LT"))
  private val registration2: IndividualRegistration = IndividualRegistration(GroupId("g2"), RegistrationNumber("12LT002"), RegimeId("LT"))

  "query registrations with a group id" should {

    "produce registration with the given group id" in {
      insertRegistration(registration1)
      insertRegistration(registration2)
      await(regRepo.count) shouldBe 2
      await(regRepo.findRegistrations(GroupId("g1"), RegimeId("LT"))) map (_.groupId) should contain theSameElementsAs (List(GroupId("g1")))
    }
  }

  "inserting more than one registration with the same group id" should {
    "result in an error" in {
      insertRegistration(registration1)
      intercept[DatabaseException](insertRegistration(registration1))
    }
  }

}
