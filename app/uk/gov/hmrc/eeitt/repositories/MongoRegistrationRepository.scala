package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Registration

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

trait RegistrationRepository {
  def lookupRegistration(groupId: String): Future[List[Registration]]
  def check(groupId: String, formId: String): Future[List[Registration]]
}

class MongoRegistrationRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Registration, BSONObjectID]("registrations", mongo, Registration.oFormat) with RegistrationRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("groupId" -> IndexType.Ascending), name = Some("groupId"), unique = true, sparse = false))
    ))
  }

  override def lookupRegistration(groupId: String): Future[List[Registration]] = {
    Logger.debug(s"lookup registration with group id '$groupId' in database ${collection.db.name}")
    find(("groupId", groupId))
  }

  override def check(groupId: String, formId: String): Future[List[Registration]] = {
    Logger.debug(s"lookup registration with group id '$groupId' and form id '$formId' in database ${collection.db.name}")
    find(
      "groupId" -> groupId,
      "formIds" -> Json.obj("$elemMatch" -> Json.obj("$eq" -> formId))
    )
  }

}

