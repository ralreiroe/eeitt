package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.Json
import play.api.mvc.Action
import reactivemongo.api.commands.{MultiBulkWriteResult, Upserted, WriteError}
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.eeitt.services.{EtmpDataParser, LineParsingException}
import uk.gov.hmrc.eeitt.utils.NonFatalWithLogging
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait EtmpDataLoaderController extends BaseController {

  def businessUserRepo: EtmpBusinessUsersRepository

  def agentRepo: EtmpAgentRepository

  def loadBusinessUsers = load(EtmpDataParser.parseFileWithBusinessUsers, businessUserRepo.replaceAll)

  def loadAgents = load(EtmpDataParser.parseFileWithAgents, agentRepo.replaceAll)

  def loadBusinessUsersDryRun = load(EtmpDataParser.parseFileWithBusinessUsers, dryRun)

  def loadAgentsDryRun = load(EtmpDataParser.parseFileWithAgents, dryRun)

  def load[A](parseFile: String => Seq[A], replaceAll: Seq[A] => Future[MultiBulkWriteResult]) =
    Action.async(parse.tolerantText) { implicit request =>
      Try(parseFile(request.body)) match {
        case Success(records) =>
          replaceAll(records).map { writeResult =>
            val expectedNumberOfInserts = records.size
            if (writeResult.ok && writeResult.n == expectedNumberOfInserts) {
              Created(Json.obj("message" -> s"$expectedNumberOfInserts unique objects imported successfully"))
            } else {
              InternalServerError(
                Json.obj(
                  "message" -> s"Failed to replace existing records with $expectedNumberOfInserts new ones",
                  "details" -> writeResult.toString
                )
              )
            }
          }

        case Failure(LineParsingException(msg)) =>
          Future.successful(BadRequest(Json.obj("message" -> msg)))

        case Failure(NonFatalWithLogging(e)) =>
          Future.successful(InternalServerError(Json.obj("message" -> e.getMessage)))
      }
    }


  def dryRun[A] (records: Seq[A]) : Future[MultiBulkWriteResult] =
      Future.successful(MultiBulkWriteResult(true, records.size, 0, Seq.empty[Upserted], Seq.empty[WriteError],
        None, None, Some("hello"), 0))

}

object EtmpDataLoaderController extends EtmpDataLoaderController {
  val businessUserRepo = etmpBusinessUserRepository
  val agentRepo = etmpAgentRepository
}
