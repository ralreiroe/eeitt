package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.EtmpAgent
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait EtmpAgentRepositorySupport extends UnitSpec with MongoSpecSupport {
  val agentRepo = new MongoEtmpAgentRepository

  def insertAgent(etmpBusinessUser: EtmpAgent): Unit = {
    val g = EtmpAgent(etmpBusinessUser.arn)
    await(agentRepo.collection.insert(g))
  }

  def awaitAgentIndexCreation() = {
    var keepChecking = true
    while (keepChecking) {
      val indexes = Await.result(agentRepo.collection.indexesManager.list(), 5.seconds)
      if (indexes.exists(_.eventualName == "arn")) {
        keepChecking = false
      }
    }
  }

}
