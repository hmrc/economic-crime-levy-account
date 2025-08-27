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

package uk.gov.hmrc.economiccrimelevyaccount.connectors

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{DataEnrichment, DateRange, FinancialDataHIP, HipRequest, SelectionCriteria, TaxpayerInformation}
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, EclReference}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, StringContextOps}

import java.time.{Instant, LocalDate}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends BaseConnector
    with Logging {

  def getFinancialDetails(
    eclReference: EclReference
  )(implicit hc: HeaderCarrier): Future[FinancialDataHIP] = {

    val correlationId = UUID.randomUUID().toString
    val hipHeaders    = buildHIPHeaders(correlationId)

    val url                    = s"${appConfig.hipUrl}/etmp/RESTAdapter/cross-regime/taxpayer/financial-data/query"
    val hipRequest: HipRequest = hipRequestBody(eclReference)
    val jsonBody               = Json.toJson(hipRequest)
    //need to delete before going live
    logger.info(s"Request Body for financial data ECL-HIP --> $jsonBody")

    httpClient
      .post(url"$url")
      .setHeader(hipHeaders: _*)
      .withBody(jsonBody)
      .executeAndDeserialise[FinancialDataHIP]
      .map { financialDataHIP =>
        logger.info(s"Successfully retrieved financial data for ECL-HIP reference--> ${eclReference.value}")
        //need to delete before going live
        logger.info(s"Response financial data for ECL-HIP API--> ${Json.toJson(financialDataHIP)}")
        financialDataHIP
      }
  }

  private def buildHIPHeaders(correlationId: String): Seq[(String, String)] = Seq(
    HeaderNames.authorisation             -> s"Basic ${appConfig.hipToken}",
    appConfig.hipServiceOriginatorIdKeyV1 -> appConfig.hipServiceOriginatorIdV1,
    CustomHeaderNames.hipCorrelationId    -> correlationId,
    CustomHeaderNames.xOriginatingSystem  -> "MDTP",
    CustomHeaderNames.xReceiptDate        -> DateTimeFormatter.ISO_INSTANT.format(
      Instant.now().truncatedTo(ChronoUnit.SECONDS)
    ),
    CustomHeaderNames.xTransmittingSystem -> "HIP"
  )

  private def hipRequestBody(eclReference: EclReference): HipRequest =
    HipRequest(
      taxRegime = "ECL",
      taxpayerInformation = TaxpayerInformation(
        idType = "ZECL",
        idNumber = eclReference.value
      ),
      targetedSearch = None,
      selectionCriteria = Some(
        SelectionCriteria(
          dateRange = DateRange(
            dateType = "POSTING",
            dateFrom = appConfig.hipDateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE),
            dateTo = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
          ),
          includeClearedItems = true,
          includeStatisticalItems = true,
          includePaymentOnAccount = true
        )
      ),
      dataEnrichment = Some(
        DataEnrichment(
          addRegimeTotalisation = true,
          addLockInformation = true,
          addPenaltyDetails = true,
          addPostedInterestDetails = true,
          addAccruingInterestDetails = true
        )
      )
    )
}
