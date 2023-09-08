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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

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
    extends Logging {

  private val loggerContext = "IntegrationFrameworkConnector"

  def getFinancialDetails(
    eclRegistrationReference: String
  )(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, Option[FinancialDataResponse]]] =
    httpClient
      .get(url"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL")
      .setHeader(integrationFrameworkHeaders: _*)
      .transform(_.addQueryStringParameters(financialDetailsQueryParams: _*))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK        =>
            logger.info(
              s"$loggerContext  - Successful response with status ${response.status} from Integration Framework for eclRegistrationReference: $eclRegistrationReference"
            )

            Right(Some(response.asInstanceOf[FinancialDataResponse]))
          case NOT_FOUND =>
            logger.info(
              s"$loggerContext  - Successful response with status ${response.status} from Integration Framework for eclRegistrationReference: $eclRegistrationReference"
            )

            Right(None)
          case _         =>
            logger.error(
              s"$loggerContext - Unsuccessful response from Integration Framework with response: ${response.body}"
            )
            Left(response.asInstanceOf[UpstreamErrorResponse])
        }
      }

  private def financialDetailsQueryParams: Seq[(String, String)] = Seq(
    (QueryParams.DATE_FROM, LocalDate.of(2023, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.DATE_TO, LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE)),
    (QueryParams.ACCRUING_INTEREST, "true"),
    (QueryParams.CLEARED_ITEMS, "true"),
    (QueryParams.LOCK_INFORMATION, "true"),
    (QueryParams.PAYMENT, "false"),
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
