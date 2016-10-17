package uk.gov.hmrc.eeitt

import play.modules.reactivemongo.ReactiveMongoPlugin

package object repositories {
  private implicit val connection = {
    import play.api.Play.current
    ReactiveMongoPlugin.mongoConnector.db
  }

  lazy val EnrolmentRepository = new EnrolmentRepository
}
