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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import play.api.http.HeaderNames
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.CustomHeaderNames
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{FinancialDataErrorResponse, FinancialDataResponse}
import uk.gov.hmrc.economiccrimelevyaccount.utils.CorrelationIdGenerator
import uk.gov.hmrc.http.{HttpClient, HttpResponse}
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._

import scala.concurrent.Future

class IntegrationFrameworkConnectorSpec extends SpecBase {
  val mockHttpClient: HttpClient                         = mock[HttpClient]
  val mockCorrelationIdGenerator: CorrelationIdGenerator = mock[CorrelationIdGenerator]
  val connector                                          = new IntegrationFrameworkConnector(appConfig, mockHttpClient, mockCorrelationIdGenerator)

  "getFinancialDetails" should {
    "return financial details when the http client returns financial details" in forAll {
      (
        eclRegistrationReference: String,
        correlationId: String,
        eitherResult: Either[FinancialDataErrorResponse, FinancialDataResponse]
      ) =>
        val expectedUrl =
          s"${appConfig.integrationFrameworkUrl}/penalty/financial-data/ZECL/$eclRegistrationReference/ECL"

        val expectedHeaders: Seq[(String, String)] = Seq(
          (HeaderNames.AUTHORIZATION, s"Bearer ${appConfig.integrationFrameworkBearerToken}"),
          (CustomHeaderNames.Environment, appConfig.integrationFrameworkEnvironment),
          (CustomHeaderNames.CorrelationId, correlationId)
        )

        when(mockCorrelationIdGenerator.generateCorrelationId).thenReturn(correlationId)

        when(
          mockHttpClient.doGet(
            ArgumentMatchers.eq(expectedUrl),
            ArgumentMatchers.eq(expectedHeaders)
          )(any())
        ).thenReturn(Future.successful(HttpResponse(200, "")))

        when(
          mockHttpClient.GET[Either[FinancialDataErrorResponse, FinancialDataResponse]](
            ArgumentMatchers.eq(expectedUrl),
            any(),
            ArgumentMatchers.eq(expectedHeaders)
          )(any(), any(), any())
        )
          .thenReturn(Future.successful(eitherResult))

        val result = await(connector.getFinancialDetails(eclRegistrationReference))

        result shouldBe eitherResult

        verify(mockHttpClient, times(1))
          .GET[Either[FinancialDataErrorResponse, FinancialDataResponse]](
            ArgumentMatchers.eq(expectedUrl),
            any(),
            ArgumentMatchers.eq(expectedHeaders)
          )(any(), any(), any())

        reset(mockHttpClient)
    }
  }
}
