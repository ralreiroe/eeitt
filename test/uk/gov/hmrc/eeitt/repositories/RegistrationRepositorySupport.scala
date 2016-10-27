package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.Registration
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationRepositorySupport extends UnitSpec with MongoSpecSupport {
  val repo = new MongoRegistrationRepository

  def insertRegistration(registration: Registration): Unit = {
    val g = Registration(registration.groupId, registration.regimeIds, registration.registrationNumber, registration.postcode)
    await(repo.collection.insert(g))
  }

}
