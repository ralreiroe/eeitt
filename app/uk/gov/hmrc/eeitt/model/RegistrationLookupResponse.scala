package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ Format, Json }

case class RegistrationLookupResponse(error: Option[String], regimeIds: Seq[String])

object RegistrationLookupResponse {
  implicit val registrationLookupResponseFormat: Format[RegistrationLookupResponse] = Json.format[RegistrationLookupResponse]
  val RESPONSE_NOT_FOUND = this(Some(Messages("registration.lookup.not.found.msg")), Nil)
  val MULTIPLE_FOUND = this(Some(Messages("registration.lookup.multiple.found.msg")), Nil)
}
