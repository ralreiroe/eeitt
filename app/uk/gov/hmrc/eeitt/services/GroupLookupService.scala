package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.GroupLookupResponse
import uk.gov.hmrc.eeitt.repositories.{ GroupRepository, groupRepository }

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait GroupLookupService {

  def groupRepo: GroupRepository

  def lookup(groupId: String): Future[GroupLookupResponse] =
    groupRepo.lookupGroup(groupId).map {
      case Nil => GroupLookupResponse(Some("not found"), None)
      case x :: Nil => GroupLookupResponse(None, Some(x))
      case x :: xs => GroupLookupResponse(Some("multiple found"), None)
    }
}

object GroupLookupService extends GroupLookupService {
  lazy val groupRepo = groupRepository
}

