package uk.gov.hmrc.eeitt.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.{ Arn, EtmpAgent, EtmpBusinessUser, RegistrationNumber }
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessUserRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with EtmpFixtures {

  "Checking if agent exists in the db" should {
    "return a nonempty list of agents if at least one agent existed" in {
      insert(testEtmpBusinessUser().copy(registrationNumber = RegistrationNumber("123")))

      assert(repo.findAll().futureValue.nonEmpty)
    }
  }

  val repo = new MongoEtmpBusinessUsersRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def insert(bu: EtmpBusinessUser) = await(repo.collection.insert(bu))

}
