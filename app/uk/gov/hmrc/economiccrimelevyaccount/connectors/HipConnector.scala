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

import io.lemonlabs.uri.{QueryString, Url}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.connectors.httpParsers.FinancialDetailsHttpHIPParser.{FinancialTransactionsFailureResponse, FinancialTransactionsHIPResponse}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework._
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, EclReference, QueryParams}
import uk.gov.hmrc.http.HttpExceptions._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, StringContextOps}

import java.time.{Instant, LocalDate}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipConnector @Inject()(
  appConfig: AppConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends BaseConnector
    with Logging {


  def getFinancialDetails(
    eclReference: EclReference
  )(implicit hc: HeaderCarrier): Future[FinancialTransactionsHIPResponse] = {

    val correlationId = UUID.randomUUID().toString
    val hipHeaders = buildHIPHeaders(correlationId)

    val url = s"${appConfig.hipUrl}/etmp/RESTAdapter/cross-regime/taxpayer/financial-data/query"
    //LNIC - Need Bhanu's models val requestBody : FinancialRequestHIP = FinancialRequestHIPHelper.HIPRequestBody("ZECL", queryParameters)
    val jsonBody = Json.toJson("")

    httpClient
      .post(url"$url")
      .setHeader(hipHeaders: _*)
      .withBody(jsonBody)
      .execute[FinancialTransactionsHIPResponse]
      .recover{
        case ex: Exception =>
          logger.warn(s"[HIPConnector][getFinancialDetails] HIP HTTP exception received: ${ex.getMessage}")
          Left(FinancialTransactionsFailureResponse(INTERNAL_SERVER_ERROR))
      }
  }

  private def buildHIPHeaders(correlationId: String): Seq[(String, String)] = Seq(
      "Authorization" -> s"Basic ${appConfig.hipToken}",
      appConfig.hipServiceOriginatorIdKeyV1 -> appConfig.hipServiceOriginatorIdV1,
      "correlationid" -> correlationId,
      "X-Originating-System" -> "MDTP",
      "X-Receipt-Date"       -> DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS)),
      "X-Transmitting-System" -> "HIP"
  )


}
