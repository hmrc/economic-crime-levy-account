/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json._
case class FinancialDataErrorResponse(errorCode: Option[String], reason: Option[String])

object FinancialDataErrorResponse {

  implicit val reads: Reads[FinancialDataErrorResponse] = (
    (JsPath \ "failures" \ "code").readNullable[String] and
      (JsPath \ "failures" \ "reason").readNullable[String]
  )(FinancialDataErrorResponse.apply _)

  implicit val writes: OWrites[FinancialDataErrorResponse] = Json.format[FinancialDataErrorResponse]
}
