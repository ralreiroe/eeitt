package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Json, OFormat }
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class Enrolment(formTypeRef: String, registrationNumber: String, livesInTheUk: Boolean, postcode: String, arn: String)

object Enrolment {
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val oFormat: OFormat[Enrolment] = Json.format[Enrolment]
}

