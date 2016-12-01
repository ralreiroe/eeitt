package uk.gov.hmrc.eeitt.controllers

import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.Action
import reactivemongo.api.commands.{ MultiBulkWriteResult, Upserted, WriteError }
import uk.gov.hmrc.eeitt.model.{ EtmpAgent, EtmpBusinessUser }
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.{ AuditService, EtmpDataParser, LineParsingException }
import uk.gov.hmrc.eeitt.utils.NonFatalWithLogging
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

trait EtmpDataLoaderController extends BaseController {

  def businessUserRepo: EtmpBusinessUsersRepository

  def agentRepo: EtmpAgentRepository

  def loadBusinessUsers = {
    Logger.info("Import business users - live")
    load(EtmpDataParser.parseFileWithBusinessUsers, businessUserRepo.replaceAll)
  }

  def loadAgents = {
    Logger.info("Import agents - live")
    load(EtmpDataParser.parseFileWithAgents, agentRepo.replaceAll)
  }

  def loadBusinessUsersDryRun = {
    Logger.info("Import business users - dry-run")
    load(EtmpDataParser.parseFileWithBusinessUsers, EtmpDataLoader.dryRun)
  }

  def loadAgentsDryRun = {
    Logger.info("Import agents - dry-run")
    load(EtmpDataParser.parseFileWithAgents, EtmpDataLoader.dryRun)
  }

  def load[A](parseFile: String => Seq[A], replaceAll: Seq[A] => Future[MultiBulkWriteResult]) =
    Action.async(parse.tolerantText) { implicit request =>
      EtmpDataLoader.load(request.body)(parseFile, replaceAll).map {
        case LoadOk(json) => Ok(json)
        case ServerFailure(json) => InternalServerError(json)
        case ParsingFailure(json) => BadRequest(json)
      }
    }
}

object EtmpDataLoaderController extends EtmpDataLoaderController {
  val businessUserRepo = etmpBusinessUserRepository
  val agentRepo = etmpAgentRepository
}

sealed trait EtmpDataLoaderResult {
  def json: JsObject
}

case class LoadOk(json: JsObject) extends EtmpDataLoaderResult
case class ServerFailure(json: JsObject) extends EtmpDataLoaderResult
case class ParsingFailure(json: JsObject) extends EtmpDataLoaderResult

object EtmpDataLoader {

  def dryRun[A](records: Seq[A]): Future[MultiBulkWriteResult] =
    Future.successful(MultiBulkWriteResult(true, records.size, 0, Seq.empty[Upserted], Seq.empty[WriteError], None, None, None, 0))

  def load[A](requestBody: String)(parseFile: String => Seq[A], replaceAll: Seq[A] => Future[MultiBulkWriteResult])(implicit hc: HeaderCarrier): Future[EtmpDataLoaderResult] = {
    Try(parseFile(requestBody)) match {
      case Success(records @ _ :: _) =>
        replaceAll(records).map { writeResult =>
          val expectedNumberOfInserts = records.size
          if (writeResult.ok && writeResult.n == expectedNumberOfInserts) {
            val recordCount = writeResult.n.toString
            records.headOption.map(r => r match {
              case e: EtmpAgent =>
                AuditService.sendDataLoadEvent("/etmp-data/live", Map {
                  "user-type" -> "agent"
                  "record-count" -> recordCount
                })
              case e: EtmpBusinessUser =>
                AuditService.sendDataLoadEvent("/etmp-data/live", Map {
                  "user-type" -> "business-user"
                  "record-count" -> recordCount
                })
              case _ =>
            })
            LoadOk(Json.obj("message" -> s"$expectedNumberOfInserts unique objects imported successfully"))
          } else {
            ServerFailure(
              Json.obj(
                "message" -> s"Failed to replace existing records with $expectedNumberOfInserts new ones",
                "details" -> writeResult.toString
              )
            )
          }
        }
      case Success(Nil) =>
        Future.successful(ParsingFailure(Json.obj(
          "message" -> "No single line was parsed from request body.",
          "body" -> requestBody.take(100)
        )))
      case Failure(LineParsingException(msg)) =>
        Future.successful(ParsingFailure(Json.obj("message" -> msg)))
      case Failure(NonFatalWithLogging(e)) =>
        Future.successful(ParsingFailure(Json.obj("message" -> e.getMessage)))
    }
  }
}
