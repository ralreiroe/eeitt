package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.{ Format, Json }
import reactivemongo.api.DB
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Enrolment

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentRepository(implicit mongo: () => DB) extends ReactiveRepository[Enrolment, String]("enrolments", mongo, Enrolment.oFormat, implicitly[Format[String]]) {

  def getEnrolments(): Future[List[Enrolment]] = {
    Logger.debug(s"retrieve all enrolments in database ${collection.db.name}")
    findAll()
  }

  def getEnrolmentsWithFormType(formTypeRef: String): Future[List[Enrolment]] = {
    Logger.debug(s"retrieve all enrolments for form ID '$formTypeRef' in database ${collection.db.name}")
    find(("formTypeRef", formTypeRef))
  }

  def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]] = {
    Logger.debug(s"lookup enrolment with registration number '$registrationNumber' in database ${collection.db.name}")
    find(("registrationNumber", registrationNumber))
  }

  def getEnrolmentsWithArn(maybeArn: Option[String]): Future[List[Enrolment]] = {
    Logger.debug(s"retrieve all enrolments for ARN '$maybeArn' in database ${collection.db.name}")
    find(("maybeArn", maybeArn))
  }

}
