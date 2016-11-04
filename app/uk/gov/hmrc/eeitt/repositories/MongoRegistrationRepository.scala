package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ Registration, RegistrationRequest }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait RegistrationRepository {
  def findRegistrations(groupId: String): Future[List[Registration]]
  def addRegime(registration: Registration, regimeId: String): Future[Either[String, Unit]]
  def register(registrationRequest: RegistrationRequest): Future[Either[String, Registration]]
}

class MongoRegistrationRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Registration, BSONObjectID]("registrations", mongo, Registration.oFormat) with RegistrationRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("groupId" -> IndexType.Ascending), name = Some("groupId"), unique = true, sparse = false))
    ))
  }

  def findRegistrations(groupId: String): Future[List[Registration]] = {
    Logger.debug(s"lookup registration with group id '$groupId' in database ${collection.db.name}")
    find(("groupId", groupId))
  }

  def addRegime(registration: Registration, regimeId: String): Future[Either[String, Unit]] = {
    val regimeIds = registration.regimeIds :+ regimeId
    val selector = Json.obj("groupId" -> registration.groupId)
    val modifier = Json.obj("$set" -> Json.obj("regimeIds" -> regimeIds))
    collection.update(selector, modifier) map {
      case r if r.ok => Right((): Unit)
      case r => Left(r.message)
    }
  }

  def register(rr: RegistrationRequest): Future[Either[String, Registration]] = {
    val registration = ??? // Registration(rr.groupId, List(rr.regimeId), rr.registrationNumber, rr.groupId)
    insert(registration) map {
      case r if r.ok => Right(registration)
      case r => Left(r.message)
    }
  }

}

