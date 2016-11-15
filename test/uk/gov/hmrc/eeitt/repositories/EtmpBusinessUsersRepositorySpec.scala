package uk.gov.hmrc.eeitt.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.model.EtmpBusinessUser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class EtmpBusinessUsersRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures {

  "Checking if user exists in the db" should {
    "return `true` if at least one user existed" in {
      insert(testEtmpBusinessUser().copy(registrationNumber = "regNumber", postcode = Some("postcode")))

      withClue("all users in db: " + await(repo.findAll())) {
        repo.userExists("regNumber", "postcode").futureValue shouldBe true
      }
    }
    "return `false` otherwise" in {
      val existingUsersInDb = List(
        testEtmpBusinessUser().copy(registrationNumber = "regNum", postcode = Some("otherPostcode")),
        testEtmpBusinessUser().copy(registrationNumber = "otherRegNum", postcode = Some("postcode"))
      )
      await(repo.bulkInsert(existingUsersInDb))

      repo.userExists("regNumber", "postcode").futureValue shouldBe false
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

  def testEtmpBusinessUser() = {
    def randomize(s: String) = s + "-" + Random.alphanumeric.take(10).mkString
    EtmpBusinessUser(
      randomize("registrationNumber"),
      randomize("taxRegime"),
      randomize("taxRegimeDescription"),
      randomize("organisationType"),
      randomize("organisationTypeDescription"),
      Some(randomize("organisationName")),
      Some(randomize("customerTitle")),
      Some(randomize("customerName1")),
      Some(randomize("customerName2")),
      Some(randomize("postcode")),
      randomize("countryCode")
    )

  }

}
