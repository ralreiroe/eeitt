package uk.gov.hmrc.eeitt

import uk.gov.hmrc.eeitt.model.{ Arn, GroupId, RegimeId, RegistrationAgent, RegistrationBusinessUser, RegistrationNumber }

import scala.util.Random

trait RegistrationFixtures {
  def testRegistrationAgent() = {

    def randomize(s: String) = s + "-" + Random.alphanumeric.take(10).mkString

    RegistrationAgent(
      GroupId(randomize("groupId")),
      Arn(randomize("arn"))
    )
  }

  def testRegistrationBusinessUser() = {

    def randomize(s: String) = s + "-" + Random.alphanumeric.take(10).mkString

    RegistrationBusinessUser(
      GroupId(randomize("groupId")),
      RegistrationNumber(randomize("registrationNumber")),
      RegimeId(randomize("regimeId"))
    )
  }
}
