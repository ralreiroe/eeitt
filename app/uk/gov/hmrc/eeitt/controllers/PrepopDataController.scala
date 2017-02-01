package uk.gov.hmrc.eeitt.controllers

import play.api.mvc._
import uk.gov.hmrc.eeitt.MicroserviceShortLivedCache
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

trait PrepopDataControllerHelper extends BaseController {
  def get(dataType: String, id: String) = Action.async {
    //    val x = MicroserviceShortLivedCache.fetchAndGetEntry(id, dataType)
    //    x match {
    //      case Some(s) => ???
    //      case _ => ???
    //    }
    val d: Option[String] = ???
    d match {
      case Some(jsonData) => Future.successful(Ok(jsonData))
      case None => Future.successful(NotFound)
    }
  }

  def put(dataType: String, id: String, jsonData: String) = Action.async {
    Future.successful(Ok)
  }

  def delete(dataType: String, id: String) = Action.async {
    Future.successful(Ok)
  }

}

class PrepopDataController extends PrepopDataControllerHelper {}
