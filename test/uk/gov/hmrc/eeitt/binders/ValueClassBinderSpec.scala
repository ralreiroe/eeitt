package uk.gov.hmrc.eeitt.binders

import org.scalatest.EitherValues
import play.api.mvc.PathBindable
import uk.gov.hmrc.eeitt.model.{ GroupId, RegimeId }
import uk.gov.hmrc.play.test.UnitSpec

class ValueClassBinderSpec extends UnitSpec with EitherValues {

  "ValueClassBinderSpec" should {
    "bind string to GroupId" in {
      val binder = ValueClassBinder.groupIdBinder
      binder.bind("", "group-id").right.value should be(GroupId("group-id"))
    }

    "unbind GroupId" in {
      val binder = ValueClassBinder.groupIdBinder
      binder.unbind("", GroupId("group-id")) should be("group-id")
    }

    "bind string to RegimeId" in {
      val binder = ValueClassBinder.regimeIdBinder
      binder.bind("", "regime-id").right.value should be(RegimeId("regime-id"))
    }

    "unbind RegimeId" in {
      val binder = ValueClassBinder.regimeIdBinder
      binder.unbind("", RegimeId("regime-id")) should be("regime-id")
    }
  }

}
