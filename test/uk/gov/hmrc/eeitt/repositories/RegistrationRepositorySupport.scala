package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.IndividualRegistration
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

trait RegistrationRepositorySupport extends UnitSpec with MongoSpecSupport {
  val regRepo = new MongoRegistrationRepository

  def insertRegistration(registration: IndividualRegistration): Unit = {
    await(regRepo.collection.insert(registration))
  }

  def awaitRegistrationIndexCreation() = {
    var keepChecking = true
    while (keepChecking) {
      val indexes = Await.result(regRepo.collection.indexesManager.list(), 5.seconds)
      if (indexes.exists(_.eventualName == "groupId")) {
        keepChecking = false
      }
    }
  }

}
