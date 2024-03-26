/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyaccount

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, getRequestedFor, matching, resetAllRequests, urlMatching, verify}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.prop.Tables._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.CustomHeaderNames
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Obligation, ObligationData, ObligationDetails}
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError
import uk.gov.hmrc.http.HeaderNames

import java.time.LocalDate

class ObligationDataISpec extends ISpecBase {

  s"GET ${routes.ObligationDataController.getObligationData.url}" should {
    "return 200 OK with the obligation data JSON when obligation data is returned" in {
      stubAuthorised()

      val obligationDetails = random[ObligationDetails]

      val obligationData = ObligationData(
        obligations = Seq(
          Obligation(
            identification = None,
            obligationDetails = Seq(
              obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2022-09-30")),
              obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2023-09-30"))
            )
          )
        )
      )

      stubGetObligations(obligationData)

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(obligationData)

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }

    "return 200 OK with null in the body when the obligation data is not found" in {
      stubAuthorised()

      stubObligationsNotFound()

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(None)

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }

    "return 502 BAD_GATEWAY when DES responds with an upstream error" in forAll(
      Table(
        "statusCode",
        BAD_REQUEST,
        INTERNAL_SERVER_ERROR,
        SERVICE_UNAVAILABLE
      )
    ) { (statusCode: Int) =>
      stubAuthorised()

      val errorMessage = "Bad Request"
      stubObligationsUpstreamError(statusCode, errorMessage)

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result)        shouldBe BAD_GATEWAY
      contentAsJson(result) shouldBe Json.toJson(
        ResponseError.badGateway(s"Get Obligation Data Failed - $errorMessage", statusCode)
      )

      eventually {
        verify(
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }

    "return 500 INTERNAL_SERVER_ERROR when an unexpected exception is thrown" in {
      stubAuthorised()

      stubGetObligationsUnexpectedResponse()

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result) shouldBe INTERNAL_SERVER_ERROR

      eventually {
        verify(
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }
  }

}
