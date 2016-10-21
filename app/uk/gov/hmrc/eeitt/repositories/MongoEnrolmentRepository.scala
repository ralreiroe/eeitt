package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import reactivemongo.api.DB
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Enrolment

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentRepository {
  def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]]
  def getEnrolmentsWithArn(arn: String): Future[List[Enrolment]]
}

class MongoEnrolmentRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Enrolment, BSONObjectID]("enrolments", mongo, Enrolment.oFormat) with EnrolmentRepository {

  override def ensureIndexes(implicit ec: scala.concurrent.ExecutionContext): scala.concurrent.Future[scala.Seq[scala.Boolean]] = {
    Future.sequence(Seq(
      collection.indexesManager.ensure(Index(Seq("registrationNumber" -> IndexType.Ascending), name = Some("registrationNumberIdx"), unique = true, sparse = false)),
      collection.indexesManager.ensure(Index(Seq("arn" -> IndexType.Ascending), name = Some("arnIdx"), unique = false, sparse = false))
    ))
  }

  override def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]] = {
    Logger.debug(s"lookup enrolment with registration number '$registrationNumber' in database ${collection.db.name}")
    find(("registrationNumber", registrationNumber))
  }

  override def getEnrolmentsWithArn(arn: String): Future[List[Enrolment]] = {
    Logger.debug(s"retrieve all enrolments for ARN '$arn' in database ${collection.db.name}")
    find(("arn", arn))
  }

}
