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
class Postcode(val value: String) extends AnyVal

case class RegistrationBusinessUser(groupId: GroupId, registrationNumber: RegistrationNumber, regimeId: RegimeId)
case class RegistrationAgent(groupId: GroupId, arn: Arn)

object RegistrationBusinessUser {
  implicit val oFormat: OFormat[RegistrationBusinessUser] = Json.format[RegistrationBusinessUser]
}

object RegistrationAgent {
  implicit val oFormat: OFormat[RegistrationAgent] = Json.format[RegistrationAgent]
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

object Postcode {
  def apply(value: String) = new Postcode(value)

  implicit val format: Format[Postcode] = new Format[Postcode] {
    def reads(json: JsValue): JsResult[Postcode] = {
      json match {
        case JsString(postcode) => JsSuccess(Postcode(postcode))
        case unknown => JsError(s"JsString value expected, got: $unknown")
      }
    }
    def writes(postcode: Postcode): JsValue = JsString(postcode.value)
  }
}
