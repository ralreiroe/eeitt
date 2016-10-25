package uk.gov.hmrc.eeitt.services

import uk.gov.hmrc.eeitt.model.GroupLookupResponse
import uk.gov.hmrc.eeitt.repositories.{ GroupRepository, groupRepository }
import uk.gov.hmrc.eeitt.model.GroupLookupResponse.{ RESPONSE_NOT_FOUND, MULTIPLE_FOUND }

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait GroupLookupService {

  def groupRepo: GroupRepository

  def lookup(groupId: String): Future[GroupLookupResponse] =
    groupRepo.lookupGroup(groupId).map {
      case Nil => RESPONSE_NOT_FOUND
      case x :: Nil => GroupLookupResponse(None, Some(x))
      case x :: xs => MULTIPLE_FOUND
    }
}

object GroupLookupService extends GroupLookupService {
  lazy val groupRepo = groupRepository
}

