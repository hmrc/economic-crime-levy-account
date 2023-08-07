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

package uk.gov.hmrc.economiccrimelevyaccount.models

object QueryParams {

  val ACCRUING_INTEREST   = "addAccruingInterestDetails"
  val CLEARED_ITEMS       = "includeClearedItems"
  val DATE_FROM           = "dateFrom"
  val DATE_TO             = "dateTo"
  val DATE_TYPE           = "dateType"
  val LOCK_INFORMATION    = "addLockInformation"
  val PAYMENT             = "includePaymentOnAccount"
  val PENALTY_DETAILS     = "addPenaltyDetails"
  val POSTED_INTEREST     = "addPostedInterestDetails"
  val REGIME_TOTALISATION = "addRegimeTotalisation"
  val STATISTICAL_ITEMS   = "includeStatisticalItems"
}
