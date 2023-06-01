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

package uk.gov.hmrc.economiccrimelevyaccount.models.bta

import play.api.libs.json.{Format, Json, OFormat}

import java.time.LocalDate

case class DueReturn(
  isOverdue: Boolean,
  dueDate: LocalDate,
  periodStartDate: LocalDate,
  periodEndDate: LocalDate,
  fyStartYear: String,
  fyEndYear: String
)

object DueReturn {
  implicit val format: OFormat[DueReturn] = Json.format[DueReturn]
  implicitly[Format[LocalDate]]
}

case class BtaTileData(eclRegistrationReference: String, dueReturn: Option[DueReturn])

object BtaTileData {
  implicit val format: OFormat[BtaTileData] = Json.format[BtaTileData]
}
