package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.RegistrationBusinessUser
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait RegistrationRepositorySupport extends UnitSpec with MongoSpecSupport {
  val regRepo = new MongoRegistrationBusinessUserRepository

  def insertRegistration(registration: RegistrationBusinessUser): Unit = {
    await(regRepo.collection.insert(registration))
  }

  def awaitRegistrationIndexCreation() = {
    var keepChecking = true
    while (keepChecking) {
      val indexes = Await.result(regRepo.collection.indexesManager.list(), 5.seconds)
      if (indexes.exists(_.eventualName == "groupIdAndRegimeId")) {
        keepChecking = false
      }
    }
  }

}
