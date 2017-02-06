package uk.gov.hmrc.eeitt.controllers

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.eeitt.model.PrepopulationJsonData
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

trait PrepopulationDataControllerHelper extends BaseController {
  def get(cacheId: String, formId: String) = Action.async { implicit request =>
    val fetched = MicroserviceShortLivedCache.fetchAndGetEntry[PrepopulationJsonData](cacheId, formId)
    fetched.map {
      case Some(d) => {
        Logger.info(s"""PrepopulationDataControllerHelper fetchAndGetEntry("$cacheId","$formId") returned $d""")
        Ok(Json.parse(d.data))
      }
      case None => {
        Logger.info(s"""PrepopulationDataControllerHelper fetchAndGetEntry("$cacheId","$formId") returned None""")
        NotFound
      }
    }
  }

  def put(cacheId: String, formId: String) = Action.async { implicit request =>
    val jsonData2 : String = request.body.asText.getOrElse("")
    val jsonData : String = (request.body.asJson.map{x => x.toString}).getOrElse("")
    val c = MicroserviceShortLivedCache.cache[PrepopulationJsonData](cacheId, formId, PrepopulationJsonData(jsonData))
    c.map {
      case c: CacheMap => {
        Logger.info(s"""PrepopulationDataControllerHelper cache("$cacheId","$formId","$jsonData") returned Some(CacheMap)""")
        Ok
      }
      case _ => {
        Logger.info(s"""PrepopulationDataControllerHelper cache("$cacheId","$formId","$jsonData") returned None""")
        BadRequest
      }
    }
  }

  def delete(cacheId: String) = Action.async { implicit request =>
    val c = MicroserviceShortLivedCache.remove(cacheId)
    c.map {
      case hr: HttpResponse if (hr.status == NO_CONTENT) => {
        Logger.info(s"""PrepopulationDataControllerHelper cache("$cacheId",) removed OK""")
        NoContent
      }
      case hr: HttpResponse => {
        Logger.info(s"""PrepopulationDataControllerHelper cache("$cacheId",) removed returned ${hr.status} """)
        BadRequest
      }
      case _ => {
        Logger.info(s"""PrepopulationDataControllerHelper cache("$cacheId"") remove failed""")
        BadRequest
      }
    }
  }
}

class PrepopulationDataController() extends PrepopulationDataControllerHelper {
}
