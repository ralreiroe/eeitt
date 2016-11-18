package uk.gov.hmrc.eeitt.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.{ Arn, EtmpAgent }
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

class EtmpAgentRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures with EtmpFixtures {

  "Checking if agent exists in the db" should {
    "return a nonempty list of agents if at least one agent existed" in {
      insert(testEtmpAgent().copy(arn = Arn("arn")))

      assert(repo.findByArn(Arn("arn")).futureValue.nonEmpty)
    }
    "return empty list otherwise" in {
      val arnToLookup = Arn("arn")
      insert(testEtmpAgent().copy(arn = Arn("otherArn")))

      assert(repo.findByArn(arnToLookup).futureValue.isEmpty)
    }
  }

  "Method replaceAll" should {
    "insert new agents if there were none before" in {
      val expectedAgents = (1 to 10).map(_ => testEtmpAgent())

      await(repo.replaceAll(expectedAgents))

      repo.findAll().futureValue should contain theSameElementsAs expectedAgents
    }
    "replace all existing agents with a new set of agents" in {
      val existingAgents = (1 to 10).map(_ => testEtmpAgent())
      await(repo.bulkInsert(existingAgents))
      val newAgents = (1 to 5).map(_ => testEtmpAgent())

      await(repo.replaceAll(newAgents))

      repo.findAll().futureValue should contain theSameElementsAs newAgents
    }
  }

  val repo = new MongoEtmpAgentRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def insert(EtmpAgent: EtmpAgent) = await(repo.collection.insert(EtmpAgent))

}
