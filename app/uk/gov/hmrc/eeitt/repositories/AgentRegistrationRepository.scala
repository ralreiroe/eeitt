package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ RegistrationAgent, GroupId, RegisterAgentRequest, RegisterBusinessUserRequest }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait RegistrationAgentRepository {
  def findRegistrations(groupId: GroupId): Future[List[RegistrationAgent]]

  def register(rr: RegisterAgentRequest): Future[Either[String, Unit]]
}

class MongoRegistrationAgentRepository(implicit mongo: () => DB)
    extends ReactiveRepository[RegistrationAgent, BSONObjectID]("registrationAgents", mongo, RegistrationAgent.oFormat) with RegistrationAgentRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("groupId" -> IndexType.Ascending), name = Some("groupId"), unique = true, sparse = false))
    ))
  }

  def findRegistrations(groupId: GroupId): Future[List[RegistrationAgent]] = {
    Logger.debug(s"lookup agent registration with group id '${groupId.value}' in database ${collection.db.name}")
    find(("groupId", groupId))
  }

  def register(rr: RegisterAgentRequest): Future[Either[String, Unit]] = {

    val registration = RegistrationAgent(rr.groupId, rr.arn)
    insert(registration) map {
      case r if r.ok => Right((): Unit)
      case r => Left(r.message)
    }
  }

}
