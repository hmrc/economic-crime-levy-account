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
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData
import uk.gov.hmrc.http.{HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future
import scala.util.{Failure, Try}

class DesConnectorSpec extends SpecBase {
  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  val connector                          = new DesConnector(appConfig, mockHttpClient, config, actorSystem)

  override def beforeEach(): Unit = {
    reset(mockRequestBuilder)
    reset(mockHttpClient)
  }

  "getObligationData" should {
    "return obligation data when the http client returns obligation data" in forAll {
      (eclReference: EclReference, obligationData: ObligationData) =>
        val expectedUrl =
          s"${appConfig.desUrl}/enterprise/obligation-data/zecl/${eclReference.value}/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"

        when(mockHttpClient.get(ArgumentMatchers.eq(url"$expectedUrl"))(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(OK, Json.stringify(Json.toJson(obligationData)))))

        val result = await(connector.getObligationData(eclReference))

        result shouldBe obligationData
    }

    "retries when a 500x error is returned from DES" in forAll {
      (
        eclReference: EclReference
      ) =>
        beforeEach()

        val errorMessage = "internal server error"
        when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, errorMessage)))

        Try(await(connector.getObligationData(eclReference))) match {
          case Failure(UpstreamErrorResponse(msg, _, _, _)) =>
            msg shouldEqual errorMessage
          case _                                            => fail("expected UpstreamErrorResponse when an error is received from DES")
        }

        verify(mockRequestBuilder, times(2))
          .execute(any(), any())
    }
  }

}
