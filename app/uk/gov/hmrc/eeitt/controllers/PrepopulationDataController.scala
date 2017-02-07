package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

trait PrepopulationDataControllerHelper extends BaseController {
  def get(cacheId: String, formId: String) = Action.async { implicit request =>
    MicroserviceShortLivedCache.fetchAndGetEntry[JsValue](cacheId, formId).map {
      case Some(d) =>
        Ok(d)
      case None =>
        NotFound
    }
  }

  def put(cacheId: String, formId: String) = Action.async(parse.json) { implicit request =>
    MicroserviceShortLivedCache.cache[JsValue](cacheId, formId, request.body).map(_ => Ok)
  }

  def delete(cacheId: String) = Action.async { implicit request =>
    MicroserviceShortLivedCache.remove(cacheId).map {
      case hr: HttpResponse if (hr.status == NO_CONTENT) =>
        NoContent
      case _ =>
        BadRequest
    }
  }
}

class PrepopulationDataController() extends PrepopulationDataControllerHelper {
}
