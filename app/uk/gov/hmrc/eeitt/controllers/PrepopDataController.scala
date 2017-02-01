package uk.gov.hmrc.eeitt.controllers

import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.eeitt.model.PrepopulationJsonData
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

trait PrepopDataControllerHelper extends BaseController {
  def get(formId: String, cacheId: String) = Action.async { implicit request =>
    MicroserviceShortLivedCache.fetchAndGetEntry[PrepopulationJsonData](cacheId, formId).map {
      case Some(d) => Ok(d.data)
      case None => NotFound
    }
  }

  def put(formId: String, cacheId: String, jsonData: String) = Action.async { implicit request =>
    MicroserviceShortLivedCache.cache[PrepopulationJsonData](cacheId, formId, PrepopulationJsonData(jsonData)).map {
      case c: CacheMap => Ok
      case _ => BadRequest
    }
  }

}

class PrepopDataController extends PrepopDataControllerHelper {}
