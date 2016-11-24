package uk.gov.hmrc.eeitt

import uk.gov.hmrc.eeitt.model.Postcode
import uk.gov.hmrc.eeitt.services.{ GetPostcode, AddRegistration, FindRegistration, FindUser }

import scala.concurrent.Future

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

  def addRegistration[A](returnValue: Either[String, Unit])(checks: A => Unit): AddRegistration[A] =
    new AddRegistration[A] {
      def apply(req: A): Future[Either[String, Unit]] = {
        checks(req)
        Future.successful(returnValue)
      }
    }

  def getPostcode[A](returnValue: Option[Postcode])(checks: A => Unit): GetPostcode[A] =
    new GetPostcode[A] {
      def apply(req: A): Option[Postcode] = {
        checks(req)
        returnValue
      }
    }
}
