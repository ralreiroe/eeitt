package uk.gov.hmrc.eeitt.repositories

import uk.gov.hmrc.eeitt.model.Group
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global

trait GroupRepositorySupport extends UnitSpec with MongoSpecSupport {
  val repo = new MongoGroupRepository

  def insertGroup(group: Group): Unit = {
    val g = Group(group.groupId, group.regimes)
    await(repo.collection.insert(g))
  }

}
