package uk.gov.hmrc.eeitt.controllers

import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.eeitt.model.PrepopulationJsonData
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

trait PrepopulationDataControllerHelper extends BaseController {
  def get(formId: String, cacheId: String) = Action.async { implicit request =>
    val fetched = MicroserviceShortLivedCache.fetchAndGetEntry[PrepopulationJsonData](cacheId, formId)
    fetched.map {
      case Some(d) => {
        Logger.info(s"""PrepopulationDataControllerHelper fetchAndGetEntry("$cacheId","$formId") returned $d""")
        Ok(d.data)
      }
      case None => {
        Logger.info(s"""PrepopulationDataControllerHelper fetchAndGetEntry("$cacheId","$formId") returned None""")
        NotFound
      }
    }
  }

  def put(formId: String, cacheId: String, jsonData: String) = Action.async { implicit request =>
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

}

class PrepopulationDataController() extends PrepopulationDataControllerHelper {
}
