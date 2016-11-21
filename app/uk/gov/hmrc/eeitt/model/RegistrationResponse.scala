package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ JsObject, JsString, Json, Writes }

case class RegistrationResponse(error: Option[String])

object RegistrationResponse {
  implicit val registrationResponseWrites: Writes[RegistrationResponse] = Json.writes[RegistrationResponse]
  val RESPONSE_OK = this(None)
  val INCORRECT_KNOWN_FACTS_BUSINESS_USERS = this(Some(Messages("registration.incorrect.known.facts.business.users.msg")))
  val INCORRECT_KNOWN_FACTS_AGENTS = this(Some(Messages("registration.incorrect.known.facts.agents.msg")))
  val MULTIPLE_FOUND = this(Some(Messages("verification.response.multiple.found.msg")))
  val INCORRECT_POSTCODE = this(Some(Messages("verification.response.incorrect.postcode.msg")))
  val ALREADY_REGISTERED = this(Some(Messages("registration.already.registered")))
  val IS_AGENT = this(Some(Messages("registration.is.registered.as.agent")))
  val IS_NOT_AGENT = this(Some(Messages("registration.is.registered.as.not_agent")))
}
