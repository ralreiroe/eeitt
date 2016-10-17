package uk.gov.hmrc.eeitt.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

class MicroserviceHelloWorldSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest("GET", "/hello")

  "GET /hello" should {
    "return 200 and text pong" in {
      val result = MicroserviceHelloWorld.hello()(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) shouldBe "HelloWorld"
    }
  }

}
