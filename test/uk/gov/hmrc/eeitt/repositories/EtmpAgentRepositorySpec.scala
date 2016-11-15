package uk.gov.hmrc.eeitt.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.eeitt.model.EtmpAgent
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class EtmpAgentRepositorySpec extends UnitSpec with MongoSpecSupport with BeforeAndAfterEach with ScalaFutures {

  "Checking if agent exists in the db" should {
    "return `true` if at least one agent existed" in {
      insert(testEtmpAgent().copy(arn = "arn"))

      repo.agentExists("arn").futureValue shouldBe true
    }
    "return `false` otherwise" in {
      val arnToLookup = "arn"
      insert(testEtmpAgent().copy(arn = "otherArn"))

      repo.agentExists(arnToLookup).futureValue shouldBe false
    }
  }

  "Method replaceAll" should {
    "insert new agents if there were none before" in {
      val expectedAgents = (1 to 10).map(_ => testEtmpAgent())

      await(repo.replaceAll(expectedAgents))

      repo.findAll().futureValue shouldBe expectedAgents
    }
    "replace all existing agents with a new set of agents" in {
      val existingAgents = (1 to 10).map(_ => testEtmpAgent())
      await(repo.bulkInsert(existingAgents))
      val newAgents = (1 to 5).map(_ => testEtmpAgent())

      await(repo.replaceAll(newAgents))

      repo.findAll().futureValue shouldBe newAgents
    }
  }

  val repo = new MongoEtmpAgentRepository

  override protected def beforeEach(): Unit = {
    await(repo.removeAll())
  }

  def insert(EtmpAgent: EtmpAgent) = await(repo.collection.insert(EtmpAgent))

  def testEtmpAgent() = {
    def randomize(s: String) = s + "-" + Random.alphanumeric.take(10).mkString
    EtmpAgent(
      randomize("arn"),
      randomize("identificationType"),
      randomize("identificationTypeDescription"),
      randomize("organisationType"),
      randomize("organisationTypeDescription"),
      Some(randomize("organisationName")),
      Some(randomize("title")),
      Some(randomize("name1")),
      Some(randomize("name2")),
      Some(randomize("postcode")),
      randomize("countryCode"),
      customers = Seq()
    )
  }

}
