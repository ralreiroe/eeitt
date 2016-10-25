package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Json, OFormat }
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class Group(groupId: String, regimes: Seq[String])

object Group {
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val oFormat: OFormat[Group] = Json.format[Group]
}

