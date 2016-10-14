package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json, OFormat }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.util.matching.Regex

case class Enrolment(id: BSONObjectID, formId: String, registrationId: String, postalCode: String) {
  def is = Enrolment.table.contains(this)

  def isRegistrationNumberOk = registrationId.contains(formId)

  val postCodeRegex = """^(GIR ?0AA|[A-PR-UWYZ]([0-9]{1,2}|([A-HK-Y][0-9]([0-9ABEHMNPRV-Y])?)|[0-9][A-HJKPS-UW]) ?[0-9][ABD-HJLNP-UW-Z]{2})$"""

  def isValidPostCode = new Regex(postCodeRegex).findFirstIn(this.postalCode).isDefined

}

object Enrolment {
  val table = List(Enrolment(BSONObjectID.generate, "1", "1", "PO197XF"), Enrolment(BSONObjectID.generate, "2", "2", "2"), Enrolment(BSONObjectID.generate, "3", "3", "3"))
  private implicit val BSONObjectIDFormat = ReactiveMongoFormats.objectIdFormats
  implicit val mongoFormats: Format[Enrolment] = Json.format[Enrolment]
  implicit val oFormat: OFormat[Enrolment] = Json.format[Enrolment]
}

