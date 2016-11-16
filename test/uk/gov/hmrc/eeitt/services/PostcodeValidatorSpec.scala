package uk.gov.hmrc.eeitt.services

import org.scalatest.{ FreeSpec, Matchers }
import uk.gov.hmrc.eeitt.services.PostcodeValidator.postcodeValidOrNotNeeded
import uk.gov.hmrc.eeitt.utils.CountryCodes

import scala.util.Random

class PostcodeValidatorSpec extends FreeSpec with Matchers {

  implicit val postcodeValidator = new PostcodeValidator[Person] {
    def countryCode(a: Person): String = a.countryCode
    def postcode(a: Person): Option[String] = a.postcode
  }

  "Postcode should be considered" - {
    "valid if it matches a reference postcode for someone from the UK (whitespace and case shouldn't matter)" in {
      val postcodesToCompare = List("E4 9RT", "E4 9RT", "e4 9rt", " E4 9RT ", "E4 9rt").map(Some.apply)
      val p = Person(postcodesToCompare.head, CountryCodes.GB)

      postcodesToCompare.foreach { postcode =>
        withClue(s"comparing ${postcodesToCompare.head} with $postcode") {
          postcodeValidOrNotNeeded(p, postcode) shouldBe true
        }
      }
    }
    "valid regardless of its value for someone from outside of the UK" in {
      val samePostcode = randomPostcode()
      val otherPostcode = randomPostcode()
      val p = Person(samePostcode, "NOT_GB")

      postcodeValidOrNotNeeded(p, samePostcode) shouldBe true
      postcodeValidOrNotNeeded(p, otherPostcode) shouldBe true
    }
    "invalid if it doesn't match a reference postcode for someone from the UK" in {
      val p = Person(randomPostcode(), CountryCodes.GB)

      postcodeValidOrNotNeeded(p, Some("some_other_non_matching_postcode")) shouldBe false
    }
  }

  case class Person(postcode: Option[String], countryCode: String)

  def randomPostcode() = Some(Random.alphanumeric.take(10).mkString)

}

