package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.http.cache.client.{ CacheMap, ShortLivedCache }
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

trait PrepopulationDataControllerHelper extends BaseController {

  def shortLivedCache: ShortLivedCache

  def get(cacheId: String, formId: String) = Action.async { implicit request =>
    shortLivedCache.fetchAndGetEntry[JsValue](cacheId, formId).map {
      case Some(d) =>
        Ok(d)
      case None =>
        NoContent
    }
  }

  def put(cacheId: String, formId: String) = Action.async(parse.json) { implicit request =>
    shortLivedCache.cache[JsValue](cacheId, formId, request.body).map(_ => Ok)
  }

  def delete(cacheId: String) = Action.async { implicit request =>
    shortLivedCache.remove(cacheId).map {
      case hr: HttpResponse if (hr.status == NO_CONTENT) =>
        NoContent
      case _ =>
        BadRequest
    }
  }
}

class PrepopulationDataController(cache: ShortLivedCache) extends PrepopulationDataControllerHelper {
  val shortLivedCache = cache
}
