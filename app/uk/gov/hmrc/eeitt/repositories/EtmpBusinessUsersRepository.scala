package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import reactivemongo.api.{ DB, ReadPreference }
import reactivemongo.api.commands.{ MultiBulkWriteResult, WriteResult }
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ EtmpBusinessUser, RegistrationNumber }
import uk.gov.hmrc.eeitt.utils.{ Diff, Differ }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait EtmpBusinessUsersRepository {
  def findByRegistrationNumber(registrationNumber: RegistrationNumber): Future[List[EtmpBusinessUser]]
  def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult]
  def report(existingRecords: Seq[EtmpBusinessUser]): Future[JsObject]
}

class MongoEtmpBusinessUsersRepository(implicit mongo: () => DB)
    extends ReactiveRepository[EtmpBusinessUser, BSONObjectID]("etmpBusinessUsers", mongo, Json.format[EtmpBusinessUser])
    with EtmpBusinessUsersRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
//    collection.indexesManager.ensure(
//      Index(
//        key = List("registrationNumber" -> IndexType.Ascending, "postcode" -> IndexType.Ascending),
//        background = true,
//        sparse = false
//      )
//    ).map(Seq(_))
    Future.successful(Seq(true))
  }

  def findByRegistrationNumber(registrationNumber: RegistrationNumber) = {
    Logger.info(s"lookup etmp business user by registration number '${registrationNumber.value}' in database ${collection.db.name}")
    find("registrationNumber" -> registrationNumber.value)
  }

  // todo: if this method fails EEITT may fail to work...
  // todo: use a correct WriteConcern
  def replaceAll(users: Seq[EtmpBusinessUser]): Future[MultiBulkWriteResult] = {
    val all: Future[WriteResult] = removeAll()
    all.onComplete {
      case (t) => {
        println("'''")

        t.foreach(println)
      }
    }
    all.flatMap { resultOfRemoving =>
      println(resultOfRemoving)
      if (resultOfRemoving.ok) {
        bulkInsert(users)
      } else {
        throw new Exception("Failed to replace users")
      }
    }
  }

  def report1(newRecords: Seq[EtmpBusinessUser]): Future[JsObject] = {

    Try(findAll()) match {
      case Success(eventualBusinessUsers) => {
        val res: Future[List[EtmpBusinessUser]] = eventualBusinessUsers
        res.map {
          case (existing) =>
            val diff: Diff[RegistrationNumber] = Differ.diff[EtmpBusinessUser, RegistrationNumber](existing, newRecords, _.registrationNumber)
            Json.obj("added" -> diff.added.size, "changed" -> diff.changed.size, "deleted" -> diff.removed.size)
        }
      }
      case Failure(ex) => Future.successful(Json.obj(
        "message" -> "failure to diff",
        "details" -> ex.toString
      ))
    }
  }

  def report0(newRecords: Seq[EtmpBusinessUser]): Future[JsObject] = {

    findAll().map {
      case (existing) =>
        val diff: Diff[RegistrationNumber] = Differ.diff[EtmpBusinessUser, RegistrationNumber](existing, newRecords, _.registrationNumber)
        Json.obj("added" -> diff.added.size, "changed" -> diff.changed.size, "deleted" -> diff.removed.size)
    }
  }

  def report2(newRecords: Seq[EtmpBusinessUser]): Future[JsObject] = {

    try {
      findAll().map {
        case (existing) =>
          val diff: Diff[RegistrationNumber] = Differ.diff[EtmpBusinessUser, RegistrationNumber](existing, newRecords, _.registrationNumber)
          Json.obj("added" -> diff.added.size, "changed" -> diff.changed.size, "deleted" -> diff.removed.size)
      }
    } catch {
      case ex: Exception => {
        Logger.error("Failure during diff", ex)
        Future.successful(Json.obj(
          "message" -> "failure to diff",
          "details" -> ex.toString
        ))
      }
    }
  }

  def report(newRecords: Seq[EtmpBusinessUser]): Future[JsObject] = {

    findAll().map {
      case (existing) =>
        val diff: Diff[RegistrationNumber] = Differ.diff[EtmpBusinessUser, RegistrationNumber](existing, newRecords, _.registrationNumber)
        Json.obj("added" -> diff.added.size, "changed" -> diff.changed.size, "deleted" -> diff.removed.size)
    } //recover {
//      case ex: Exception => {
//        Logger.error(s"Failure during diff: $ex")
//        Json.obj()
//      }
//    }
  }

}
