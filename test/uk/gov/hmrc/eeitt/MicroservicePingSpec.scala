package uk.gov.hmrc.eeitt.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

class MicroservicePingSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest("GET", "/ping")

  "GET /ping" should {
    "return 200 and text pong" in {
      val result = MicroservicePing.ping()(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe "pong"
    }
  }

}
