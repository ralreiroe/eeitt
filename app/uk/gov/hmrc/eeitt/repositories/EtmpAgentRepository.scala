package uk.gov.hmrc.eeitt.repositories

import play.api.libs.json.Json
import reactivemongo.api.commands.MultiBulkWriteResult
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.api.{ DB, ReadPreference }
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.eeitt.model.{ EtmpAgent, Arn }
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait EtmpAgentRepository {
  def findByArn(arn: Arn): Future[List[EtmpAgent]]
  def replaceAll(users: Seq[EtmpAgent]): Future[MultiBulkWriteResult]
}

class MongoEtmpAgentRepository(implicit mongo: () => DB)
    extends ReactiveRepository[EtmpAgent, BSONObjectID]("etmpAgents", mongo, Json.format[EtmpAgent])
    with EtmpAgentRepository {

  override def ensureIndexes(implicit ec: ExecutionContext) = {
    collection.indexesManager.ensure(
      Index(
        key = List("arn" -> IndexType.Ascending),
        background = true,
        sparse = false
      )
    ).map(Seq(_))
  }

  def findByArn(arn: Arn) = find("arn" -> arn)

  // todo: if this method fails EEITT may fail to work...
  // todo: use a correct WriteConcern
  def replaceAll(users: Seq[EtmpAgent]): Future[MultiBulkWriteResult] = {
    removeAll().flatMap { resultOfRemoving =>
      if (resultOfRemoving.ok) {
        bulkInsert(users)
      } else {
        throw new Exception("Failed to replace users")
      }
    }
  }

}
