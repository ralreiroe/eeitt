package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.{ RegisterRequest, Postcode }

trait GetPostcode[A] {
  def apply(a: A): Option[Postcode]
}

object GetPostcode {
  implicit val getPostcode = new GetPostcode[RegisterRequest] {
    def apply(req: RegisterRequest): Option[Postcode] = req.postcode
  }
}
