package uk.gov.hmrc.eeitt.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.{ EtmpBusinessUser, RegistrationNumber }
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EtmpBusinessUsersRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with EtmpFixtures {

  "Checking if user exists in the db" should {
    "return a non empty list of users if at least one user existed with a given reg number exists" in {
      insert(testEtmpBusinessUser().copy(registrationNumber = "regNumber"))

      withClue("all users in db: " + await(repo.findAll())) {
        assert(repo.findByRegistrationNumber(RegistrationNumber("regNumber")).futureValue.nonEmpty)
      }
    }
    "return empty list otherwise" in {
      await(insert(testEtmpBusinessUser().copy(registrationNumber = "otherRegNum")))

      assert(repo.findByRegistrationNumber(RegistrationNumber("regNumber")).futureValue.isEmpty)
    }
  }

  "Method replaceAll" should {
    "insert new users if there were none before" in {
      val expectedUsers = (1 to 10).map(_ => testEtmpBusinessUser())

      await(repo.replaceAll(expectedUsers))

      repo.findAll().futureValue should contain theSameElementsAs expectedUsers
    }
    "replace all existing users with a new set of users" in {
      val existingUsers = (1 to 10).map(_ => testEtmpBusinessUser())
      await(repo.bulkInsert(existingUsers))
      val newUsers = (1 to 5).map(_ => testEtmpBusinessUser())

      await(repo.replaceAll(newUsers))

      repo.findAll().futureValue should contain theSameElementsAs newUsers
    }
  }

  val repo = new MongoEtmpBusinessUsersRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def insert(etmpBusinessUser: EtmpBusinessUser) = await(repo.collection.insert(etmpBusinessUser))

}
