package uk.gov.hmrc.eeitt.utils

import play.api.Logger

import scala.util.control.NonFatal

object NonFatalWithLogging {
  def unapply(t: Throwable) = {
    val result = NonFatal.unapply(t)
    result.foreach { _ =>
      Logger.debug(t.getMessage, t)
    }
    result
  }
}
