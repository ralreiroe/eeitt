package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ Format, Json }

case class RegistrationLookupResponse(error: Option[String], registration: Option[Registration])

object RegistrationLookupResponse {
  implicit val registrationLookupResponseFormat: Format[RegistrationLookupResponse] = Json.format[RegistrationLookupResponse]
  val RESPONSE_NOT_FOUND = this(Some(Messages("registration.not.found.msg")), None)
  val MULTIPLE_FOUND = this(Some(Messages("registration.multiple.found.msg")), None)
}
