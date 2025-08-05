/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyaccount.models.hip

import play.api.libs.json.{Json, OFormat}

case class TaxpayerInformation(
  idType: String,
  idNumber: String
)

object TaxpayerInformation {
  implicit val format: OFormat[TaxpayerInformation] = Json.format[TaxpayerInformation]
}

case class TargetedSearch(
  searchType: String,
  searchItem: String
)

object TargetedSearch {
  implicit val format: OFormat[TargetedSearch] = Json.format[TargetedSearch]
}

case class DateRange(
  dateType: String,
  dateFrom: String,
  dateTo: String
)

object DateRange {
  implicit val format: OFormat[DateRange] = Json.format[DateRange]
}

case class SelectionCriteria(
  dateRange: DateRange,
  includeClearedItems: Boolean,
  includeStatisticalItems: Boolean,
  includePaymentOnAccount: Boolean
)

object SelectionCriteria {
  implicit val format: OFormat[SelectionCriteria] = Json.format[SelectionCriteria]
}

case class DataEnrichment(
  addRegimeTotalisation: Boolean,
  addLockInformation: Boolean,
  addPenaltyDetails: Boolean,
  addPostedInterestDetails: Boolean,
  addAccruingInterestDetails: Boolean
)

object DataEnrichment {
  implicit val format: OFormat[DataEnrichment] = Json.format[DataEnrichment]
}

case class HipRequest(
  taxRegime: String,
  taxpayerInformation: TaxpayerInformation,
  targetedSearch: Option[TargetedSearch],
  selectionCriteria: Option[SelectionCriteria],
  dataEnrichment: Option[DataEnrichment]
)

object HipRequest {
  implicit val format: OFormat[HipRequest] = Json.format[HipRequest]
}
