package uk.gov.hmrc.eeitt

import scala.concurrent.Future
import uk.gov.hmrc.eeitt.model.AuditData
import uk.gov.hmrc.eeitt.services.{ AddRegistration, FindRegistration, FindUser }
import uk.gov.hmrc.eeitt.typeclasses.HmrcAudit
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.eeitt.checks.{ AddRegistrationCheck, AuditCheck, FindRegistrationCheck, FindUserCheck }

trait TypeclassFixtures {
  class FindRegistrationTC[B](callCheck: Option[FindRegistrationCheck], returnValue: List[B]) {
    def callCheck(callCheck: FindRegistrationCheck) = new FindRegistrationTC(Some(callCheck), returnValue)
    def response(r: List[B]) = new FindRegistrationTC(callCheck, r)

    def withChecks[A](f: A => Unit) = getTC(Some(f))
    def noChecks[A] = getTC[A](None)

    private def getTC[A](checkFn: Option[A => Unit] = None): FindRegistration.Aux[A, B] = new FindRegistration[A] {
      type Out = B
      def apply(req: A): Future[List[B]] = {
        callCheck.foreach(_.call())
        checkFn.foreach(_(req))
        Future.successful(returnValue)
      }
    }
  }

  object FindRegistrationTC {
    def callCheck[A](callCheck: FindRegistrationCheck) = new FindRegistrationTC(Some(callCheck), List.empty[A])
    def response[A](r: List[A]) = new FindRegistrationTC(None, r)

    def noChecks[A] = new FindRegistrationTC(None, List.empty[A]).noChecks[A]
    def withChecks[A](f: A => Unit) = new FindRegistrationTC(None, List.empty[A]).withChecks(f)
  }

  class FindUserTC[B](callCheck: Option[FindUserCheck], returnValue: List[B]) {
    def callCheck(callCheck: FindUserCheck) = new FindUserTC(Some(callCheck), returnValue)
    def response(r: List[B]) = new FindUserTC(callCheck, r)

    def withChecks[A](f: A => Unit) = getTC(Some(f))
    def noChecks[A] = getTC[A](None)

    private def getTC[A](checkFn: Option[A => Unit] = None): FindUser[A, B] = new FindUser[A, B] {
      def apply(req: A): Future[List[B]] = {
        callCheck.foreach(_.call())
        checkFn.foreach(_(req))
        Future.successful(returnValue)
      }
    }
  }

  object FindUserTC {
    def callCheck[A](callCheck: FindUserCheck) = new FindUserTC(Some(callCheck), List.empty[A])
    def response[A](r: List[A]) = new FindUserTC(None, r)

    def noChecks[A, B] = new FindUserTC(None, List.empty[B]).noChecks[A]
    def withChecks[A, B](f: A => Unit) = new FindUserTC(None, List.empty[B]).withChecks(f)
  }

  class AddRegistrationTC(callCheck: Option[AddRegistrationCheck], returnValue: Either[String, Unit]) {
    def callCheck(callCheck: AddRegistrationCheck) = new AddRegistrationTC(Some(callCheck), returnValue)
    def response(r: Either[String, Unit]) = new AddRegistrationTC(callCheck, r)

    def withChecks[A](f: A => Unit) = getTC(Some(f))
    def noChecks[A] = getTC[A](None)

    private def getTC[A](checkFn: Option[A => Unit] = None): AddRegistration[A] = new AddRegistration[A] {
      def apply(req: A): Future[Either[String, Unit]] = {
        callCheck.foreach(_.call())
        checkFn.foreach(_(req))
        Future.successful(returnValue)
      }
    }
  }

  object AddRegistrationTC {
    def callCheck[A](callCheck: AddRegistrationCheck) = new AddRegistrationTC(Some(callCheck), Right(()))
    def response(r: Either[String, Unit]) = new AddRegistrationTC(None, r)

    def noChecks[A] = new AddRegistrationTC(None, Right(())).noChecks[A]
    def withChecks[A](f: A => Unit) = new AddRegistrationTC(None, Right(())).withChecks(f)
  }

  class HmrcAuditTC(callCheck: Option[AuditCheck]) {

    def withChecks(f: AuditData => Unit) = getTC(Some(f))
    def noChecks = getTC(None)

    private def getTC(checkFn: Option[AuditData => Unit] = None): HmrcAudit[AuditData] = new HmrcAudit[AuditData] {
      override def apply(ad: AuditData): HeaderCarrier => Unit = hc => {
        callCheck.foreach(_.call())
        checkFn.foreach(_(ad))
      }
    }
  }

  object HmrcAuditTC {
    def callCheck(callCheck: AuditCheck) = new HmrcAuditTC(Some(callCheck))

    def noChecks = new HmrcAuditTC(None).noChecks
    def withChecks(f: AuditData => Unit) = new HmrcAuditTC(None).withChecks(f)
  }
}
