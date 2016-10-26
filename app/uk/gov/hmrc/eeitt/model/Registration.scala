package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Json, OFormat }
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class Registration(groupId: String, regimeIds: Seq[String], registrationNumber: String, postcode: String)

object Registration {
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val oFormat: OFormat[Registration] = Json.format[Registration]
}

