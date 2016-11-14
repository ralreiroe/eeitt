package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json.Json
import play.api.libs.openid.Errors.AUTH_CANCEL
import play.api.mvc.Action
import reactivemongo.api.commands.MultiBulkWriteResult
import uk.gov.hmrc.eeitt.model.EtmpAgent
import uk.gov.hmrc.eeitt.repositories._
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.eeitt.services.{ EtmpAgentRecordParser, EtmpBusinessUserParser }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait EtmpDataLoaderController extends BaseController {

  def businessUserRepo: EtmpBusinessUsersRepository

  def agentRepo: EtmpAgentRepository

  def loadBusinessUsers = load(EtmpBusinessUserParser.parseFile, businessUserRepo.replaceAll)

  def loadAgents = {
    val parseRecords = EtmpAgentRecordParser.parseFile _
    val textToEtmpAgents = parseRecords.andThen(EtmpAgent.fromRecords)
    load(textToEtmpAgents, agentRepo.replaceAll)
  }

  def load[A](parseFile: String => Seq[A], replaceAll: Seq[A] => Future[MultiBulkWriteResult]) =
    Action.async(parse.tolerantText) { implicit request =>
      val records = parseFile(request.body)
      val expectedNumberOfInserts = records.size
      replaceAll(records).map { writeResult =>
        if (writeResult.ok && writeResult.n == expectedNumberOfInserts) {
          Created(Json.obj("message" -> s"$expectedNumberOfInserts records imported successfully"))
        } else {
          InternalServerError(
            Json.obj(
              "message" -> s"Failed to replace existing records with $expectedNumberOfInserts new ones",
              "details" -> writeResult.toString
            )
          )
        }
      } // todo recover from exception inside of the parser
    }

}

object EtmpDataLoaderController extends EtmpDataLoaderController {
  val businessUserRepo = etmpBusinessUserRepository
  val agentRepo = etmpAgentRepository
}
