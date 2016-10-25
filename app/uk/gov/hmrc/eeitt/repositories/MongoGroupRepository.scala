package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Group

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

trait GroupRepository {
  def lookupGroup(groupId: String): Future[List[Group]]
}

class MongoGroupRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Group, BSONObjectID]("groups", mongo, Group.oFormat) with GroupRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("groupId" -> IndexType.Ascending), name = Some("groupId"), unique = true, sparse = false))
    ))
  }

  override def lookupGroup(groupId: String): Future[List[Group]] = {
    Logger.debug(s"lookup group with group id '$groupId' in database ${collection.db.name}")
    find(("groupId", groupId))
  }

}
