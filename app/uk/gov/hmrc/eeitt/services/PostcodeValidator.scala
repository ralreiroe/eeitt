package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpBusinessUser }
import uk.gov.hmrc.eeitt.utils.CountryCodes

trait PostcodeValidator[A] {
  def countryCode(a: A): String
  def postcode(a: A): Option[String]
}

object PostcodeValidator {
  implicit object UserPostcodeValidator extends PostcodeValidator[EtmpBusinessUser] {
    def countryCode(a: EtmpBusinessUser) = a.countryCode
    def postcode(a: EtmpBusinessUser) = a.postcode
  }

  implicit object AgentPostcodeValidator extends PostcodeValidator[EtmpAgent] {
    def countryCode(a: EtmpAgent) = a.countryCode
    def postcode(a: EtmpAgent) = a.postcode
  }

  private def isFromTheUk(countryCode: String): Boolean = countryCode == CountryCodes.GB

  private def normalize(s: String) = s.replaceAll("\\s", "").toLowerCase

  def postcodeValidOrNotNeeded[A](a: A, postcodeFromRequest: Option[String])(implicit validator: PostcodeValidator[A]): Boolean = {
    if (isFromTheUk(validator.countryCode(a))) {
      validator.postcode(a).map(normalize) == postcodeFromRequest.map(normalize)
    } else {
      true
    }
  }
}
