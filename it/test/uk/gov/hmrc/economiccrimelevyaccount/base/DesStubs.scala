/*
 * Copyright 2026 HM Revenue & Customs
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

package test.uk.gov.hmrc.economiccrimelevyaccount.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData

import java.time.{LocalDate, ZoneOffset}

trait DesStubs { self: WireMockStubs =>

  def stubGetObligations(obligationData: ObligationData): StubMapping =
    stub(
      get(
        urlEqualTo(
          s"/enterprise/obligation-data/zecl/${testEclReference.value}/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"
        )
      ),
      aResponse()
        .withStatus(OK)
        .withBody(Json.toJson(obligationData).toString())
    )

  def stubObligationsNotFound(): StubMapping =
    stub(
      get(
        urlEqualTo(
          s"/enterprise/obligation-data/zecl/${testEclReference.value}/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"
        )
      ),
      aResponse()
        .withStatus(NOT_FOUND)
        .withBody("No obligation data found")
    )

  def stubObligationsUpstreamError(statusCode: Int, body: String): StubMapping =
    stub(
      get(
        urlEqualTo(
          s"/enterprise/obligation-data/zecl/${testEclReference.value}/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"
        )
      ),
      aResponse()
        .withStatus(statusCode)
        .withBody(body)
    )

  def stubGetObligationsUnexpectedResponse(): StubMapping =
    stub(
      get(
        urlEqualTo(
          s"/enterprise/obligation-data/zecl/${testEclReference.value}/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"
        )
      ),
      aResponse()
        .withStatus(OK)
        .withBody(Json.toJson("").toString())
    )

}
