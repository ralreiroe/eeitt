package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ Format, Json }

case class GroupLookupResponse(error: Option[String], group: Option[Group])

object GroupLookupResponse {
  implicit val groupLookupResponseFormat: Format[GroupLookupResponse] = Json.format[GroupLookupResponse]
  val RESPONSE_NOT_FOUND = this(Some(Messages("group.response.not.found.msg")), None)
  val MULTIPLE_FOUND = this(Some(Messages("group.response.multiple.found.msg")), None)
}
