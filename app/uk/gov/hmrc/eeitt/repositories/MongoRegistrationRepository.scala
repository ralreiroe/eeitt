package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Json
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ GroupId, RegistrationBusinessUser, RegimeId, RegisterAgentRequest, RegisterBusinessUserRequest }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait RegistrationRepository {
  def findRegistrations(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationBusinessUser]]

  def register(registrationRequest: RegisterBusinessUserRequest): Future[Either[String, Unit]]
}

class MongoRegistrationBusinessUserRepository(implicit mongo: () => DB)
    extends ReactiveRepository[RegistrationBusinessUser, BSONObjectID]("registrationBusinessUsers", mongo, RegistrationBusinessUser.oFormat) with RegistrationRepository {

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(
      Seq(
        collection.indexesManager.ensure(
          Index(
            key = List("groupId" -> IndexType.Ascending, "regimeId" -> IndexType.Ascending),
            name = Some("groupIdAndRegimeId"),
            unique = true,
            sparse = false
          )
        )
      )
    )
  }

  def findRegistrations(groupId: GroupId, regimeId: RegimeId): Future[List[RegistrationBusinessUser]] = {
    Logger.info(s"lookup business user registration with group id '${groupId.value}' and regime id ${regimeId.value} in database ${collection.db.name}")
    find(
      "groupId" -> groupId,
      "regimeId" -> regimeId
    )
  }

  def register(rr: RegisterBusinessUserRequest): Future[Either[String, Unit]] = {
    val registration = RegistrationBusinessUser(rr.groupId, rr.registrationNumber, rr.regimeId)
    insert(registration) map {
      case r if r.ok =>
        Logger.info(s"registration of business user groupId ${rr.groupId.value} for regime ${rr.regimeId.value} successful. Registration number ${rr.registrationNumber.value}, postcode: ${rr.postcode}")
        Right(())
      case r =>
        Logger.error(s"registration of business user groupId ${rr.groupId.value} for regime ${rr.regimeId.value} failed. Registration number ${rr.registrationNumber.value}, postcode: ${rr.postcode}. Reason: ${r.message}.")
        Left(r.message)
    }
  }
}
