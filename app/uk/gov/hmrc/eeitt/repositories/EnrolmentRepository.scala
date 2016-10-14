package uk.gov.hmrc.eeitt.repositories

import play.api.Logger
import play.api.libs.json.{ Format, JsObject, Json, OFormat }
import reactivemongo.api.DB
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Enrolment(id: BSONObjectID, formId: String, registrationId: String, postalCode: String)

object Enrolment {
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val mongoFormats: Format[Enrolment] = Json.format[Enrolment]
  implicit val oFormat: OFormat[Enrolment] = Json.format[Enrolment]
}

class EnrolmentRepository(implicit mongo: () => DB) extends ReactiveRepository[Enrolment, String]("enrolments", mongo, Enrolment.mongoFormats, implicitly[Format[String]]) {

  def getAllEnrolments(): Future[List[Enrolment]] = withCurrentTime { now =>
    Logger.debug(s"retrieve all enrolments")
    collection.find(Json.obj()).cursor[Enrolment].collect[List]()
  }

  def getAllEnrolmentsWithFormId(formId: String): Future[List[Enrolment]] = withCurrentTime { now =>
    Logger.debug(s"retrieve all enrolments for form ID '$formId'")
    collection.find(Json.obj("formId" -> formId)).cursor[Enrolment].collect[List]()
  }

}
