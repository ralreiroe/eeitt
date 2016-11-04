package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class VerificationResponse(isAllowed: Boolean)

object VerificationResponse {
  implicit val checkResponseFormat: Format[VerificationResponse] = Json.format[VerificationResponse]
}
