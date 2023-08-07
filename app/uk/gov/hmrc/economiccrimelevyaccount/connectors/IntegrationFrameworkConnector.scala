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
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, QueryParams}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework._
import uk.gov.hmrc.economiccrimelevyaccount.utils.CorrelationIdGenerator
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.format.DateTimeFormatter
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IntegrationFrameworkConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClient,
  correlationIdGenerator: CorrelationIdGenerator
)(implicit ec: ExecutionContext)
    extends Logging {

  def getFinancialDetails(
    eclRegistrationReference: String
  )(implicit hc: HeaderCarrier): Future[Either[FinancialDataErrorResponse, FinancialDataResponse]] =
    httpClient.GET[Either[FinancialDataErrorResponse, FinancialDataResponse]](
      s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL",
      headers = integrationFrameworkHeaders,
      queryParams = financialDetailsQueryParams
    )

  private def financialDetailsQueryParams: Seq[(String, String)] = Seq(
    (QueryParams.CLEARED_ITEMS, "true"),
    (QueryParams.PAYMENT, "true"),
    (QueryParams.STATISTICAL_ITEMS, "true"),
    (QueryParams.DATE_TYPE, "POSTING"),
    (QueryParams.DATE_FROM, LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.DATE_TO, LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE))
  )

  private def integrationFrameworkHeaders: Seq[(String, String)] = Seq(
    (HeaderNames.AUTHORIZATION, s"Bearer ${appConfig.integrationFrameworkBearerToken}"),
    (CustomHeaderNames.Environment, appConfig.integrationFrameworkEnvironment),
    (CustomHeaderNames.CorrelationId, correlationIdGenerator.generateCorrelationId)
  )
}
