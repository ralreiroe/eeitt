package uk.gov.hmrc.eeitt.repositories

import play.api.libs.json.Json
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.api.{ DB, ReadPreference }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.EtmpBusinessUser
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait EtmpBusinessUsersRepository {
  def userExists(registrationNumber: String, postcode: String): Future[Boolean]
  def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult]
}

class MongoEtmpBusinessUsersRepository(implicit mongo: () => DB)
    extends ReactiveRepository[EtmpBusinessUser, BSONObjectID]("etmpBusinessUsers", mongo, Json.format[EtmpBusinessUser])
    with EtmpBusinessUsersRepository {

  override def ensureIndexes(implicit ec: ExecutionContext) = {
    collection.indexesManager.ensure(
      Index(
        key = List("registrationNumber" -> IndexType.Ascending, "postcode" -> IndexType.Ascending),
        background = true,
        sparse = false
      )
    ).map(Seq(_))
  }

  def userExists(registrationNumber: String, postcode: String): Future[Boolean] = {
    collection
      .find(Json.obj("registrationNumber" -> registrationNumber, "postcode" -> postcode))
      .cursor[EtmpBusinessUser](ReadPreference.secondaryPreferred)
      .collect[List]()
      .map(_.nonEmpty)
  }

  // todo: if this method fails EEITT may fail to work...
  // todo: use a correct WriteConcern
  def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult] = {
    removeAll().flatMap { resultOfRemoving =>
      if (resultOfRemoving.ok) {
        bulkInsert(users)
      } else {
        throw new Exception("Failed to replace users")
      }
    }
  }

}