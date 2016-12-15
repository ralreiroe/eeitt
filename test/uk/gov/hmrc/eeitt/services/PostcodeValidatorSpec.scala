package uk.gov.hmrc.eeitt.services

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.hmrc.eeitt.EtmpFixtures
import uk.gov.hmrc.eeitt.model.Postcode
import uk.gov.hmrc.eeitt.services.PostcodeValidator.postcodeValidOrNotNeeded
import uk.gov.hmrc.eeitt.utils.CountryCodes

import scala.util.Random

class PostcodeValidatorSpec extends FreeSpec with Matchers with EtmpFixtures {

  implicit val postcodeValidator = new PostcodeValidator[Person] {
    def countryCode(a: Person): Option[String] = a.countryCode
    def postcode(a: Person): Option[Postcode] = a.postcode
  }

  "Postcode should be considered" - {
    "valid if it matches a reference postcode for someone from the UK (whitespace and case shouldn't matter)" in {
      val postcodesToCompare = List("E4 9RT", "E4 9RT", "e4 9rt", " E4 9RT ", "E4 9rt").map(Postcode.apply).map(Some.apply)
      val p = Person(postcodesToCompare.head, Some(CountryCodes.GB))

      postcodesToCompare.foreach { postcode =>
        withClue(s"comparing ${postcodesToCompare.head} with $postcode") {
          postcodeValidOrNotNeeded(p, postcode) shouldBe true
        }
      }
    }
    "valid regardless of its value for someone from outside of the UK" in {
      val samePostcode = randomPostcode()
      val otherPostcode = randomPostcode()
      val p = Person(samePostcode, Some("NOT_GB"))

      postcodeValidOrNotNeeded(p, samePostcode) shouldBe true
      postcodeValidOrNotNeeded(p, otherPostcode) shouldBe true
    }
    "invalid if it doesn't match a reference postcode for someone from the UK" in {
      val p = Person(randomPostcode(), Some(CountryCodes.GB))

      postcodeValidOrNotNeeded(p, Some(Postcode("some_other_non_matching_postcode"))) shouldBe false
    }

    "valid for agent from UK" in {
      val agent = testEtmpAgent().copy(postcode = Some(Postcode("12345")), countryCode = Some(CountryCodes.GB))
      postcodeValidOrNotNeeded(agent, Some(Postcode("12345"))) shouldBe true
    }

    "valid for business user from UK" in {
      val businessUser = testEtmpBusinessUser().copy(postcode = Some(Postcode("12345")), countryCode = Some(CountryCodes.GB))
      postcodeValidOrNotNeeded(businessUser, Some(Postcode("12345"))) shouldBe true
    }
  }

  case class Person(postcode: Option[Postcode], countryCode: Option[String])

  def randomPostcode() = Some(Postcode(Random.alphanumeric.take(10).mkString))

}
