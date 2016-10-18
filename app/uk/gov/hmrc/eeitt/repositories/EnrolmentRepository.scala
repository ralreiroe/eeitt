package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.Format
import reactivemongo.api.DB
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.eeitt.model.Enrolment

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentRepository(implicit mongo: () => DB) extends ReactiveRepository[Enrolment, String]("enrolments", mongo, Enrolment.oFormat, implicitly[Format[String]]) {

  def getAllEnrolments(): Future[List[Enrolment]] = withCurrentTime { now =>
    Logger.debug(s"retrieve all enrolments in database ${collection.db.name}")
    findAll()
  }

  def getAllEnrolmentsWithFormType(formTypeRef: String): Future[List[Enrolment]] = withCurrentTime { now =>
    Logger.debug(s"retrieve all enrolments for form ID '$formTypeRef' in database ${collection.db.name}")
    find(("formTypeRef", formTypeRef))
  }

  def lookupEnrolment(registrationNumber: String): Future[List[Enrolment]] = withCurrentTime { now =>
    Logger.debug(s"lookup enrolment with registration number '$registrationNumber' in database ${collection.db.name}")
    find(("registrationNumber", registrationNumber))
  }

}
