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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData
import uk.gov.hmrc.economiccrimelevyaccount.utils.CorrelationIdHelper
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class DesConnectorSpec extends SpecBase {
  val mockHttpClient: HttpClientV2                    = mock[HttpClientV2]
  val mockConfiguration: Config                       = mock[Config]
  val mockActorSystem: ActorSystem                    = mock[ActorSystem]
  val mockCorrelationIdGenerator: CorrelationIdHelper = mock[CorrelationIdHelper]
  val mockRequestBuilder: RequestBuilder              = mock[RequestBuilder]
  val connector                                       = new DesConnector(appConfig, mockHttpClient, mockConfiguration, mockActorSystem)

  override def beforeEach(): Unit = {
    reset(mockRequestBuilder)
    reset(mockHttpClient)
  }

  "getObligationData" should {
    "return obligation data when the http client returns obligation data" in forAll {
      (eclRegistrationReference: EclReference, obligationData: ObligationData) =>
        val expectedUrl =
          s"${appConfig.desUrl}/enterprise/obligation-data/zecl/$eclRegistrationReference/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"

        when(mockHttpClient.get(ArgumentMatchers.eq(url"$expectedUrl"))(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[ObligationData](any(), any()))
          .thenReturn(Future.successful(obligationData))

        val result = await(connector.getObligationData(eclRegistrationReference))

        result shouldBe obligationData
    }
  }

}
