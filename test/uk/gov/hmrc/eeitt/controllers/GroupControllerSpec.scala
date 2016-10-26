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
import uk.gov.hmrc.eeitt.model.GroupLookupResponse.{ RESPONSE_NOT_FOUND, MULTIPLE_FOUND }

import scala.concurrent.Future

class GroupControllerSpec extends UnitSpec with WithFakeApplication with MustExpectations with NumericMatchers with Mockito {

  private val group1 = Group("1", List("LT", "LL"), "12LT001", "SE39EP")
  private val group2 = Group("2", List("LT", "LL", "XT"), "12LT002", "SE39EX")

  object TestGroupLookupService extends GroupLookupService {
    val groupRepo = mock[MongoGroupRepository]
    groupRepo.lookupGroup("1").returns(Future.successful(List(group1)))
    groupRepo.lookupGroup("2").returns(Future.successful(List(group2)))
    groupRepo.lookupGroup("3").returns(Future.successful(List()))
    groupRepo.lookupGroup("4").returns(Future.successful(List(
      Group("4", List("LT", "LL"), "12LT004", "SE38ZZ"),
      Group("4", List("LT", "XT"), "12LT005", "SE39ZZ")
    )))
  }

  object TestGroupController extends GroupController {
    val groupLookupService = TestGroupLookupService
  }

  "GET /regimes/1" should {
    "return 200 and correct response for successful lookup of a group" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestGroupController.regimes("1")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(GroupLookupResponse(None, Some(group1)))
    }
  }

  "GET /regimes/3" should {
    "return 200 and error response for an unsuccessful lookup" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestGroupController.regimes("3")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(RESPONSE_NOT_FOUND)
    }
  }

  "GET /regimes/4" should {
    "return 200 and error response for a lookup which returned multiple instances" in {
      val fakeRequest = FakeRequest(Helpers.GET, "/regimes")
      val result = TestGroupController.regimes("4")(fakeRequest)
      status(result) shouldBe Status.OK
      jsonBodyOf(await(result)) shouldBe toJson(MULTIPLE_FOUND)
    }
  }

}
