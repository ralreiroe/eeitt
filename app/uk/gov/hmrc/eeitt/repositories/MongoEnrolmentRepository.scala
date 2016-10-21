package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Format
import reactivemongo.api.DB
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Enrolment

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait EnrolmentRepository {
  def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]]
  def getEnrolmentsWithArn(arn: String): Future[List[Enrolment]]
}

class MongoEnrolmentRepository(implicit mongo: () => DB)
    extends ReactiveRepository[Enrolment, String]("enrolments", mongo, Enrolment.oFormat, implicitly[Format[String]]) with EnrolmentRepository {

  override def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]] = {
    Logger.debug(s"lookup enrolment with registration number '$registrationNumber' in database ${collection.db.name}")
    find(("registrationNumber", registrationNumber))
  }

  override def getEnrolmentsWithArn(arn: String): Future[List[Enrolment]] = {
    Logger.debug(s"retrieve all enrolments for ARN '$arn' in database ${collection.db.name}")
    find(("arn", arn))
  }

}
