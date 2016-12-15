package uk.gov.hmrc.eeitt.typeclasses

import org.scalatest._
import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpBusinessUser }

class SendDataLoadEventSpec extends FlatSpec with Matchers {
  "SendDataLoadEvent" should "prepare audit data for EtmpAgent" in {
    val m = implicitly[SendDataLoadEvent[EtmpAgent]].apply(10)
    m should contain("user-type" -> "agent")
    m should contain("record-count" -> "10")
  }

  it should "prepare audit data for EtmpBusinessUser" in {
    val m = implicitly[SendDataLoadEvent[EtmpBusinessUser]].apply(10)
    m should contain("user-type" -> "business-user")
    m should contain("record-count" -> "10")
  }
}
