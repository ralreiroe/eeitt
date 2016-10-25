package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class GroupLookupResponse(error: Option[String], group: Option[Group])

object GroupLookupResponse {
  implicit val groupLookupResponseFormat: Format[GroupLookupResponse] = Json.format[GroupLookupResponse]
}
