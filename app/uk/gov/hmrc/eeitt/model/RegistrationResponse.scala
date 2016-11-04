package uk.gov.hmrc.eeitt.model

import play.api.i18n.Messages
import play.api.libs.json.{ Format, Json }

case class RegistrationResponse(error: Option[String])

object RegistrationResponse {
  implicit val registrationResponseFormat: Format[RegistrationResponse] = Json.format[RegistrationResponse]
  val RESPONSE_OK = this(None)
  val INCORRECT_KNOWN_FACTS = this(Some(Messages("registration.incorrect.known.facts.msg")))
  val RESPONSE_NOT_FOUND = this(Some(Messages("verification.response.not.found.msg")))
  val MULTIPLE_FOUND = this(Some(Messages("verification.response.multiple.found.msg")))
  val INCORRECT_REGIME = this(Some(Messages("verification.response.incorrect.regime.msg")))
  val INCORRECT_POSTCODE = this(Some(Messages("verification.response.incorrect.postcode.msg")))
  val INCORRECT_ARN = this(Some(Messages("verification.response.incorrect.arn.msg")))
  val MISSING_ARN = this(Some(Messages("verification.response.missing.arn.msg")))
  val INCORRECT_ARN_FOR_CLIENT = this(Some(Messages("verification.response.incorrect.arn.for.client.msg")))
  val ALREADY_REGISTERED = this(Some(Messages("registration.already.registered")))
}
