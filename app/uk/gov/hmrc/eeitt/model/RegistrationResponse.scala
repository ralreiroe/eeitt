package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ Format, Json }

case class RegistrationResponse(error: Option[String])

object RegistrationResponse {
  implicit val registrationResponseFormat: Format[RegistrationResponse] = Json.format[RegistrationResponse]
  val REGISTRATION_OK = this(None)
  val INCORRECT_KNOWN_FACTS = this(Some(Messages("registration.incorrect.known.facts.msg")))
  val MULTIPLE_FOUND = this(Some(Messages("registration.multiple.found.msg")))
  val ALREADY_REGISTERED = this(Some(Messages("registration.already.registered")))
}
