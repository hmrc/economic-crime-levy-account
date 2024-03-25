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
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, EclReference}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialData
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialData
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.Future
import scala.util.{Failure, Try}

class IntegrationFrameworkConnectorSpec extends SpecBase {
  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val connector                          = new IntegrationFrameworkConnector(appConfig, mockHttpClient)

  override def beforeEach(): Unit = {
    reset(mockRequestBuilder)
    reset(mockHttpClient)
  }

  "getFinancialDetails" should {
    "return financial details when the http client returns financial details" in forAll {
      (
        eclReference: EclReference,
        financialData: FinancialData,
        correlationId: String
      ) =>
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(
          mockRequestBuilder.setHeader(
            ArgumentMatchers.eq((HeaderNames.authorisation, s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
          )
        )
          .thenReturn(mockRequestBuilder)
        when(
          mockRequestBuilder.setHeader(
            ArgumentMatchers.eq((CustomHeaderNames.environment, appConfig.integrationFrameworkEnvironment))
          )
        )
          .thenReturn(mockRequestBuilder)
        when(
          mockRequestBuilder.setHeader(
            ArgumentMatchers.eq((CustomHeaderNames.correlationId, correlationId))
          )
        )
          .thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(OK, Json.stringify(Json.toJson(financialData)))))

        val headerCarrier = HeaderCarrier(otherHeaders = Seq(CustomHeaderNames.xCorrelationId -> correlationId))
        await(connector.getFinancialDetails(eclReference)(headerCarrier)).isInstanceOf[FinancialData] shouldBe true
    }

    "retries when a 500x error is returned from integration framework" in forAll {
      (
        eclReference: EclReference
      ) =>
        beforeEach()

        val errorMessage = "internal server error"
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(
          mockRequestBuilder.setHeader(
            ArgumentMatchers.eq((HeaderNames.authorisation, s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
          )
        )
          .thenReturn(mockRequestBuilder)
        when(
          mockRequestBuilder.setHeader(
            ArgumentMatchers.eq((CustomHeaderNames.environment, appConfig.integrationFrameworkEnvironment))
          )
        )
          .thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, errorMessage)))

        Try(await(connector.getFinancialDetails(eclReference))) match {
          case Failure(UpstreamErrorResponse(msg, _, _, _)) =>
            msg shouldEqual errorMessage
          case _                                            =>
            fail("expected UpstreamErrorResponse when an error is received from IF")
        }
    }
  }
}
