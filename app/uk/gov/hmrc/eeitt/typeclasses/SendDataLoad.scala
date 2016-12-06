package uk.gov.hmrc.eeitt.typeclasses

import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpBusinessUser }

trait SendDataLoadEvent[A] {
  def apply(numberOfRecords: Int): Map[String, String]
}

object SendDataLoadEvent {
  implicit object agent extends SendDataLoadEvent[EtmpAgent] {
    def apply(numberOfRecords: Int) = Map(
      "user-type" -> "agent",
      "record-count" -> numberOfRecords.toString
    )
  }

  implicit object businessUser extends SendDataLoadEvent[EtmpBusinessUser] {
    def apply(numberOfRecords: Int) = Map(
      "user-type" -> "business-user",
      "record-count" -> numberOfRecords.toString
    )
  }
}
