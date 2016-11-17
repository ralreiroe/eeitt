package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ JsObject, JsString, Json, Writes }

case class RegistrationResponse(error: Option[String])

object RegistrationResponse {
  implicit val registrationResponseWrites: Writes[RegistrationResponse] = Json.writes[RegistrationResponse].transform { jsValue =>
    jsValue match {
      case JsObject(Seq(("error", JsString(messageKey)))) => JsString(Messages(messageKey))
      case other => other
    }
  }
  val RESPONSE_OK = this(None)
  val INCORRECT_KNOWN_FACTS = this(Some("registration.incorrect.known.facts.msg"))
  val MULTIPLE_FOUND = this(Some("verification.response.multiple.found.msg"))
  val INCORRECT_POSTCODE = this(Some("verification.response.incorrect.postcode.msg"))
  val ALREADY_REGISTERED = this(Some("registration.already.registered"))
  val IS_AGENT = this(Some("registration.is.registered.as.agent"))
  val IS_NOT_AGENT = this(Some("registration.is.registered.as.not_agent"))
}
