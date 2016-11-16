/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.eeitt.binders

import play.api.libs.json.{ JsError, JsString, JsSuccess, Json }
import play.api.mvc.PathBindable
import uk.gov.hmrc.eeitt.model.{ AffinityGroup, Agent }

object AffinityGroupBinder {
  implicit def affinityGroupBinder(implicit stringBinder: PathBindable[String]) =
    new PathBindable[AffinityGroup] {
      def bind(key: String, value: String): Either[String, AffinityGroup] =
        stringBinder.bind(key, value).right.flatMap(parseAffinityGroupString)

      override def unbind(key: String, affinityGroup: AffinityGroup): String =
        stringBinder.unbind(key, affinityGroup.toString)
    }
  private def parseAffinityGroupString(affinityGroupString: String) = {
    JsString(affinityGroupString).validate[AffinityGroup] match {
      case JsSuccess(affinityGroup, _) => Right(affinityGroup)
      case JsError(_) => Left("No valid affinity group: " + affinityGroupString)
    }
  }
}
