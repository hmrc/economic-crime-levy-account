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
import play.api.http.HeaderNames
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, QueryParams}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework._
import uk.gov.hmrc.economiccrimelevyaccount.utils.CorrelationIdGenerator
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.format.DateTimeFormatter
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationFrameworkConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2,
  correlationIdGenerator: CorrelationIdGenerator
)(implicit ec: ExecutionContext)
    extends BaseConnector
    with Logging {

  private val loggerContext = "IntegrationFrameworkConnector"

  private def ifUrl(eclRegistrationReference: String) =
    s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL"

  private def ifUrl(eclRegistrationReference: String) = Url(
    path = s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL",
    query = QueryString.fromPairs("triggerId" -> messageId.value)
  ).toStringRaw

  def getFinancialDetails(
    eclRegistrationReference: String
  )(implicit hc: HeaderCarrier): Future[Option[FinancialDataResponse]] =
    httpClient
      .get(url"${ifUrl(eclRegistrationReference)}")
      .setHeader(integrationFrameworkHeaders: _*)
      .executeAndDeserialiseOption[ObligationData]

  httpClient
    .GET[HttpResponse](
      s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL",
      headers = integrationFrameworkHeaders,
      queryParams = financialDetailsQueryParams
    )
    .flatMap { response =>
      response.status match {
        case OK        => response.as[FinancialDataResponse].map(Some(_))
        case NOT_FOUND => Future.successful(None)
        case _         =>
          response.error
      }
    }

  private def financialDetailsQueryParams: Seq[(String, String)] = Seq(
    (QueryParams.DATE_FROM, LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.DATE_TO, LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.ACCRUING_INTEREST, "true"),
    (QueryParams.CLEARED_ITEMS, "true"),
    (QueryParams.LOCK_INFORMATION, "true"),
    (QueryParams.PAYMENT, "true"),
    (QueryParams.PENALTY_DETAILS, "true"),
    (QueryParams.POSTED_INTEREST, "true"),
    (QueryParams.REGIME_TOTALISATION, "true"),
    (QueryParams.STATISTICAL_ITEMS, "true"),
    (QueryParams.DATE_TYPE, "POSTING")
  )

  private def integrationFrameworkHeaders: Seq[(String, String)] = Seq(
    (HeaderNames.AUTHORIZATION, s"Bearer ${appConfig.integrationFrameworkBearerToken}"),
    (CustomHeaderNames.Environment, appConfig.integrationFrameworkEnvironment),
    (CustomHeaderNames.CorrelationId, correlationIdGenerator.generateCorrelationId)
  )
}
