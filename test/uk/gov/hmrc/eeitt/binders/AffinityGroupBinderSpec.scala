package uk.gov.hmrc.eeitt.binders

import org.scalatest.EitherValues
import play.api.mvc.PathBindable
import uk.gov.hmrc.eeitt.model.{ Agent, Individual, Organisation }
import uk.gov.hmrc.play.test.UnitSpec

class AffinityGroupBinderSpec extends UnitSpec with EitherValues {

  "AffinityGroupBinder" should {
    "bind strings 'Agent', 'Individual' and 'Organisation' to AffinityGroup" in {
      val binder = AffinityGroupBinder.affinityGroupBinder

      binder.bind("", "Agent").right.value should be(Agent)
      binder.bind("", "Individual").right.value should be(Individual)
      binder.bind("", "Organisation").right.value should be(Organisation)

      binder.bind("", "typo").left.value should be("No valid affinity group: typo")
    }

    "unbind Agent, Individual and Organisation" in {
      val binder = AffinityGroupBinder.affinityGroupBinder

      binder.unbind("", Agent) should be("Agent")
      binder.unbind("", Individual) should be("Individual")
      binder.unbind("", Organisation) should be("Organisation")
    }
  }

}
