package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ AgentRegistration, GroupId, RegisterAgentRequest, RegisterRequest }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait AgentRegistrationRepository {
  def findRegistrations(groupId: GroupId): Future[List[AgentRegistration]]

  def register(rr: RegisterAgentRequest): Future[Either[String, Unit]]
}

class MongoAgentRegistrationRepository(implicit mongo: () => DB)
    extends ReactiveRepository[AgentRegistration, BSONObjectID]("agentRegistrations", mongo, AgentRegistration.oFormat) with AgentRegistrationRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("groupId" -> IndexType.Ascending), name = Some("groupId"), unique = true, sparse = false))
    ))
  }

  def findRegistrations(groupId: GroupId): Future[List[AgentRegistration]] = {
    Logger.debug(s"lookup agent registration with group id '${groupId.value}' in database ${collection.db.name}")
    find(("groupId", groupId))
  }

  def register(rr: RegisterAgentRequest): Future[Either[String, Unit]] = {

    val registration = AgentRegistration(rr.groupId, rr.arn)
    insert(registration) map {
      case r if r.ok => Right((): Unit)
      case r => Left(r.message)
    }
  }

}
