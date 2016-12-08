package uk.gov.hmrc.eeitt

import scala.concurrent.Future
import uk.gov.hmrc.eeitt.services.{ AddRegistration, FindRegistration, FindUser }

trait TypeclassFixtures {
  def findRegistration[A, B](returnValue: List[B])(checks: A => Unit) =
    new FindRegistration[A] {
      type Out = B
      def apply(req: A): Future[List[B]] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

  def findUser[A, B](returnValue: List[B])(checks: A => Unit): FindUser[A, B] =
    new FindUser[A, B] {
      def apply(req: A): Future[List[B]] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

}
