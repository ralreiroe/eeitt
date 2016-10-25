package uk.gov.hmrc.eeitt.controllers

import org.specs2.matcher.{ MustExpectations, NumericMatchers }
import org.specs2.mock.Mockito
import play.api.http.Status
import play.api.libs.json.Json._
import play.api.test.{ FakeRequest, Helpers }
import uk.gov.hmrc.eeitt.model.{ Group, GroupLookupResponse }
import uk.gov.hmrc.eeitt.repositories.MongoGroupRepository
import uk.gov.hmrc.eeitt.services.GroupLookupService
import uk.gov.hmrc.play.test.{ UnitSpec, WithFakeApplication }

import scala.concurrent.Future

class GroupControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  object TestGroupLookupService extends GroupLookupService {
    val groupRepo = mock[MongoGroupRepository]
    groupRepo.lookupGroup("1").returns(Future.successful(List(Group("1", List("LT", "LL")))))
    groupRepo.lookupGroup("2").returns(Future.successful(List(Group("2", List("LT", "LL", "XT")))))
  }

  object TestGroupController extends GroupController {
    val groupLookupService = TestGroupLookupService
  }

  "GET /regimes/1" should {
    "return 200 and correct response for successful lookup of a group" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestGroupController.regimes("1")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(GroupLookupResponse(None, Some(Group("1", List("LT", "LL")))))
    }
  }

  //  "POST /verify with incorrect request" should {
  //    "return 400 (BadRequest) and information about errors" in {
  //      val fakeRequest = FakeRequest(Helpers.POST, "/verify").withBody(Json.obj("incorrect" -> "request"))
  //      val result = TestEnrolmentController.verify()(fakeRequest)
  //      status(result) shouldBe Status.BAD_REQUEST
  //      val errorInformation = jsonBodyOf(await(result))
  //      val messages: Seq[JsValue] = (errorInformation \\ "msg")
  //      messages.size must be_>=(1)
  //    }
  //  }
}
