package uk.gov.hmrc.eeitt.utils

case class Diff[A](added: Seq[A], removed: Seq[A], changed: Seq[A])

object Differ {

  def diff[A, B](oldl: Seq[A], newl: Seq[A], idex: A => B): Diff[B] = {
    val removedorchanged = oldl.diff(newl).map(idex)
    val addedorchanged = newl.diff(oldl).map(idex)

    val (added, removed) =
      (addedorchanged.diff(removedorchanged), removedorchanged.diff(addedorchanged))
    Diff(added, removed, addedorchanged.diff(added))
  }

}
