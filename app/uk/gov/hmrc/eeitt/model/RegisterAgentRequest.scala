package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{ Format, Json }

case class RegisterAgentRequest(groupId: GroupId, arn: Arn)

object RegisterAgentRequest {
  implicit val registerAgentRequestFormat: Format[RegisterAgentRequest] = Json.format[RegisterAgentRequest]
}
