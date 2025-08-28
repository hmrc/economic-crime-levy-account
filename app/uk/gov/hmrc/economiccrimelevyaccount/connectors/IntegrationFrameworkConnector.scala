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
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework._
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, EclReference, QueryParams}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, StringContextOps}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationFrameworkConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends BaseConnector
    with Logging {

  private def ifUrl(eclRegistrationReference: String) = Url(
    path = s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL",
    query = QueryString.fromTraversable(financialDetailsQueryParams)
  ).toStringRaw

  def getFinancialDetails(
    eclReference: EclReference
  )(implicit hc: HeaderCarrier): Future[FinancialData] = {
    val correlationId = hc.headers(scala.Seq(CustomHeaderNames.xCorrelationId)) match {
      case Seq((_, id)) =>
        id
      case _            =>
        UUID.randomUUID().toString
    }

    httpClient
      .get(url"${ifUrl(eclReference.value)}")
      .setHeader((HeaderNames.authorisation, s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
      .setHeader((CustomHeaderNames.environment, appConfig.integrationFrameworkEnvironment))
      .setHeader((CustomHeaderNames.correlationId, correlationId))
      .executeAndDeserialise[FinancialData]
      .map { financialData =>
        logger.info((s"Response financial data for ECL-IF API--> ${eclReference.value}"))
        logger.info((s"Response financial data for ECL-IF API--> ${Json.toJson(financialData)}"))
        financialData
      }

  }

  private def financialDetailsQueryParams: Seq[(String, String)] = Seq(
    (
      QueryParams.dateFrom,
      appConfig.integrationFrameworkDateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE)
    ),
    (QueryParams.dateTo, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.accruingInterest, "true"),
    (QueryParams.clearedItems, "true"),
    (QueryParams.lockInformation, "true"),
    (QueryParams.payment, "true"),
    (QueryParams.penaltyDetails, "true"),
    (QueryParams.postedInterest, "true"),
    (QueryParams.regimeTotalisation, "true"),
    (QueryParams.statisticalItesm, "true"),
    (QueryParams.dateType, "POSTING")
  )
}
