package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import reactivemongo.api.DB
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ EtmpBusinessUser, RegistrationNumber }
import uk.gov.hmrc.eeitt.utils.{ Diff, Differ }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait EtmpBusinessUsersRepository {
  def findByRegistrationNumber(registrationNumber: RegistrationNumber): Future[List[EtmpBusinessUser]]
  def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult]
  def report(existingRecords: Seq[EtmpBusinessUser]): Future[JsObject]
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

  def findByRegistrationNumber(registrationNumber: RegistrationNumber) = {
    Logger.info(s"lookup etmp business user by registration number '${registrationNumber.value}' in database ${collection.db.name}")
    find("registrationNumber" -> registrationNumber.value)
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

  def report(newRecords: Seq[EtmpBusinessUser]): Future[JsObject] = {
    findAll().map {
      case (existing) =>
        val diff: Diff[RegistrationNumber] = Differ.diff[EtmpBusinessUser, RegistrationNumber](existing, newRecords, _.registrationNumber)
        Json.obj("added" -> diff.added.size, "changed" -> diff.changed.size, "deleted" -> diff.removed.size)
    }
  }
}
