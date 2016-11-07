package uk.gov.hmrc.eeitt.repositories

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.model.EtmpBusinessUser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EtmpBusinessUsersRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures {

  "Checking if user exists in the db" should {
    "return `true` if at least one user existed" in {
      insert(EtmpBusinessUser(registrationNumber = "regNum", postcode = "postcode"))

      repo.userExists(EtmpBusinessUser(registrationNumber = "regNum", postcode = "postcode")).futureValue shouldBe true
    }
    "return `false` otherwise" in {
      val existingUsersInDb = List(
        EtmpBusinessUser("regNum", "otherPostcode"),
        EtmpBusinessUser("otherRegNum", "postcode")
      )
      await(repo.bulkInsert(existingUsersInDb))
      val userToLookUp = EtmpBusinessUser(registrationNumber = "regNum", postcode = "postcode")

      repo.userExists(userToLookUp).futureValue shouldBe false
    }
  }

  "Method replaceAll" should {
    "insert new users if there were none before" in {
      val expectedUsers = (1 to 10).map(_ => testEtmpBusinessUser())

      await(repo.replaceAll(expectedUsers))

      repo.findAll().futureValue shouldBe expectedUsers
    }
    "replace all existing users with a new set of users" in {
      val existingUsers = (1 to 10).map(_ => testEtmpBusinessUser())
      await(repo.bulkInsert(existingUsers))
      val newUsers = (1 to 5).map(_ => testEtmpBusinessUser())

      await(repo.replaceAll(newUsers))

      repo.findAll().futureValue shouldBe newUsers
    }
  }

  val repo = new MongoEtmpBusinessUsersRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def insert(etmpBusinessUser: EtmpBusinessUser) = await(repo.collection.insert(etmpBusinessUser))

  def testEtmpBusinessUser() = {
    def randomize(s: String) = s + "-" + UUID.randomUUID()
    EtmpBusinessUser(randomize("regNumber"), randomize("postcode"))
  }

}
