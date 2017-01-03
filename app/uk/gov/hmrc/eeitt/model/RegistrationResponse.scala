package uk.gov.hmrc.eeitt.model

import play.api.i18n.MessagesApi
import play.api.libs.json.{ JsObject, JsString, Json, Writes }

sealed trait RegistrationResponse {

  private def error(msg: String) = Json.obj("error" -> msg)

  def toJson(messages: play.api.i18n.Messages): JsObject = this match {
    case RESPONSE_OK => Json.obj()
    case INCORRECT_KNOWN_FACTS_BUSINESS_USERS => error(messages("registration.incorrect.known.facts.business.users.msg"))
    case INCORRECT_KNOWN_FACTS_AGENTS => error(messages("registration.incorrect.known.facts.agents.msg"))
    case MULTIPLE_FOUND => error(messages("verification.response.multiple.found.msg"))
    case INCORRECT_POSTCODE => error(messages("verification.response.incorrect.postcode.msg"))
    case ALREADY_REGISTERED => error(messages("registration.already.registered"))
    case IS_AGENT => error(messages("registration.is.registered.as.agent"))
    case IS_NOT_AGENT => error(messages("registration.is.registered.as.not_agent"))
    case Other(msg) => error(msg)

  }
}

case object RESPONSE_OK extends RegistrationResponse
case object INCORRECT_KNOWN_FACTS_BUSINESS_USERS extends RegistrationResponse
case object INCORRECT_KNOWN_FACTS_AGENTS extends RegistrationResponse
case object MULTIPLE_FOUND extends RegistrationResponse
case object INCORRECT_POSTCODE extends RegistrationResponse
case object ALREADY_REGISTERED extends RegistrationResponse
case object IS_AGENT extends RegistrationResponse
case object IS_NOT_AGENT extends RegistrationResponse
case class Other(key: String) extends RegistrationResponse
