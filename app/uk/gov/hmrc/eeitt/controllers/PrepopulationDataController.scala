package uk.gov.hmrc.eeitt.controllers

import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait PrepopulationDataControllerHelper extends BaseController {
  def get(cacheId: String, formId: String) = Action.async { implicit request =>
    val fetched = MicroserviceShortLivedCache.fetchAndGetEntry[JsValue](cacheId, formId)
    fetched.map {
      case Some(d) =>
        Ok(d)
      case None =>
        NotFound
    }
  }

  def put(cacheId: String, formId: String) = Action.async { implicit request =>
    request.body.asJson.map {
      case v: JsValue =>
        MicroserviceShortLivedCache.cache[JsValue](cacheId, formId, v).map {
          case c: CacheMap =>
            Ok
        }
    }.getOrElse(Future.successful(BadRequest))
  }

  def delete(cacheId: String) = Action.async { implicit request =>
    val c = MicroserviceShortLivedCache.remove(cacheId)
    c.map {
      case hr: HttpResponse if (hr.status == NO_CONTENT) =>
        NoContent
      case _ =>
        BadRequest
    }
  }
}

class PrepopulationDataController() extends PrepopulationDataControllerHelper {
}
