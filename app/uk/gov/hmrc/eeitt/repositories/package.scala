package uk.gov.hmrc.eeitt

import play.modules.reactivemongo.ReactiveMongoPlugin

package object repositories {
  private implicit val connection = {
    import play.api.Play.current
    ReactiveMongoPlugin.mongoConnector.db
  }

  lazy val registrationRepository = new MongoRegistrationRepository
  lazy val agentRegistrationRepository = new MongoAgentRegistrationRepository
  lazy val etmpBusinessUserRepository = new MongoEtmpBusinessUsersRepository
  lazy val etmpAgentRepository = new MongoEtmpAgentRepository
}
