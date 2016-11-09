package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Json, OFormat }

case class Registration(groupId: String, isAgent: Boolean, registrationNumber: String, arn: String, regimeIds: Seq[String])

object Registration {
  implicit val oFormat: OFormat[Registration] = Json.format[Registration]
}

