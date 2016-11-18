package uk.gov.hmrc.eeitt.model

import play.api.libs.json.{
  Json,
  OFormat,
  Format,
  JsResult,
  JsSuccess,
  JsError,
  JsString,
  JsValue
}

class GroupId(val value: String) extends AnyVal
class RegimeId(val value: String) extends AnyVal
class Arn(val value: String) extends AnyVal
class RegistrationNumber(val value: String) extends AnyVal

case class IndividualRegistration(groupId: GroupId, registrationNumber: RegistrationNumber, regimeId: RegimeId)
case class AgentRegistration(groupId: GroupId, arn: Arn)

object IndividualRegistration {
  implicit val oFormat: OFormat[IndividualRegistration] = Json.format[IndividualRegistration]
}

object AgentRegistration {
  implicit val oFormat: OFormat[AgentRegistration] = Json.format[AgentRegistration]
}

object GroupId {
  def apply(value: String) = new GroupId(value)

  implicit val format: Format[GroupId] = new Format[GroupId] {
    def reads(json: JsValue): JsResult[GroupId] = {
      json match {
        case JsString(groupId) => JsSuccess(GroupId(groupId))
        case unknown => JsError(s"JsString value expected, got: $unknown")
      }
    }
    def writes(groupId: GroupId): JsValue = JsString(groupId.value)
  }
}

object RegimeId {
  def apply(value: String) = new RegimeId(value)

  implicit val format: Format[RegimeId] = new Format[RegimeId] {
    def reads(json: JsValue): JsResult[RegimeId] = {
      json match {
        case JsString(regimeId) => JsSuccess(RegimeId(regimeId))
        case unknown => JsError(s"JsString value expected, got: $unknown")
      }
    }
    def writes(regimeId: RegimeId): JsValue = JsString(regimeId.value)
  }
}

object Arn {
  def apply(value: String) = new Arn(value)

  implicit val format: Format[Arn] = new Format[Arn] {
    def reads(json: JsValue): JsResult[Arn] = {
      json match {
        case JsString(arn) => JsSuccess(Arn(arn))
        case unknown => JsError(s"JsString value expected, got: $unknown")
      }
    }
    def writes(arn: Arn): JsValue = JsString(arn.value)
  }
}

object RegistrationNumber {
  def apply(value: String) = new RegistrationNumber(value)

  implicit val format: Format[RegistrationNumber] = new Format[RegistrationNumber] {
    def reads(json: JsValue): JsResult[RegistrationNumber] = {
      json match {
        case JsString(registrationNumber) => JsSuccess(RegistrationNumber(registrationNumber))
        case unknown => JsError(s"JsString value expected, got: $unknown")
      }
    }
    def writes(registrationNumber: RegistrationNumber): JsValue = JsString(registrationNumber.value)
  }
}
