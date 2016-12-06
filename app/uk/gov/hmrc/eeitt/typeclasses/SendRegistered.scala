package uk.gov.hmrc.eeitt.typeclasses

import uk.gov.hmrc.eeitt.model.{ Postcode, RegisterAgentRequest, RegisterBusinessUserRequest }

trait SendRegistered[A] {
  def apply(a: A): (Option[Postcode], Map[String, String])
}

object SendRegistered {

  implicit object agent extends SendRegistered[RegisterAgentRequest] {
    override def apply(request: RegisterAgentRequest): (Option[Postcode], Map[String, String]) = {
      val requestTags = Map(
        "user-type" -> "agent",
        "arn" -> request.arn.value,
        "group-id" -> request.groupId.value
      )
      (request.postcode, requestTags)
    }
  }

  implicit object businessUserv extends SendRegistered[RegisterBusinessUserRequest] {
    override def apply(request: RegisterBusinessUserRequest): (Option[Postcode], Map[String, String]) = {
      val requestTags = Map(
        "user-type" -> "business-user",
        "registration-number" -> request.registrationNumber.value,
        "group-id" -> request.groupId.value,
        "regime-id" -> request.regimeId.value
      )
      (request.postcode, requestTags)
    }
  }
}
