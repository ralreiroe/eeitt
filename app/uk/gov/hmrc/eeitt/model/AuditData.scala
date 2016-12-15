package uk.gov.hmrc.eeitt.model

class AuditData(val path: String, val postcode: Option[Postcode], val tags: Map[String, String])
