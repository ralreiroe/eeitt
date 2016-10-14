package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json, OFormat }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class Enrolment(id: BSONObjectID, formId: String, registrationId: String, postalCode: String)

object Enrolment {
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val mongoFormats: Format[Enrolment] = Json.format[Enrolment]
  implicit val oFormat: OFormat[Enrolment] = Json.format[Enrolment]
}

