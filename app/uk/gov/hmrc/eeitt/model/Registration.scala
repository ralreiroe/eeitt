package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Json, OFormat }

case class Registration(groupId: String, regimeIds: Seq[String], registrationNumber: String, postcode: String)

object Registration {
  implicit val oFormat: OFormat[Registration] = Json.format[Registration]
}

