package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.EtmpBusinessUser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait EtmpBusinessUserRepositorySupport extends UnitSpec with MongoSpecSupport {
  val userRepo = new MongoEtmpBusinessUsersRepository

  def insertBusinessUser(etmpBusinessUser: EtmpBusinessUser): Unit = {
    await(userRepo.collection.insert(etmpBusinessUser))
  }

  def awaitUserIndexCreation() = {
    var keepChecking = true
    while (keepChecking) {
      val indexes = Await.result(userRepo.collection.indexesManager.list(), 5.seconds)
      if (indexes.exists(_.eventualName == "registrationNumber") && indexes.exists(_.eventualName == "postcode")) {
        keepChecking = false
      }
    }
  }

}
