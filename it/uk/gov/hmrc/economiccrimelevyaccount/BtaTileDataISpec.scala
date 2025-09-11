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
import uk.gov.hmrc.economiccrimelevyaccount.models.bta.{BtaTileData, DueReturn}
import uk.gov.hmrc.economiccrimelevyaccount.models.des._
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError
import uk.gov.hmrc.http.HeaderNames

import java.time.LocalDate

class BtaTileDataISpec extends ISpecBase {

  s"GET ${routes.BtaTileDataController.getBtaTileData.url}" should {

    "return 200 OK with a due return when there is an open obligation" in {
      stubAuthorised()

      val openObligation =
        random[ObligationDetails].copy(
          status = Open,
          inboundCorrespondenceFromDate = LocalDate.parse("2021-04-01"),
          inboundCorrespondenceToDate = LocalDate.parse("2022-03-31"),
          inboundCorrespondenceDueDate = LocalDate.parse("2022-09-30")
        )

      val obligationData =
        ObligationData(
          Seq(Obligation(None, Seq(openObligation)))
        )

      stubGetObligations(obligationData)

      val result = callRoute(
        FakeRequest(routes.BtaTileDataController.getBtaTileData)
      )

      val expectedBtaTileData = BtaTileData(
        eclReference = testEclReference,
        dueReturn = Some(
          DueReturn(
            isOverdue = true,
            dueDate = openObligation.inboundCorrespondenceDueDate,
            periodStartDate = openObligation.inboundCorrespondenceFromDate,
            periodEndDate = openObligation.inboundCorrespondenceToDate,
            fyStartYear = "2021",
            fyEndYear = "2022"
          )
        )
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.desEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }

    "return 200 OK when there is a fulfilled obligation" in {
      resetAllRequests()
      stubAuthorised()

      val openObligation =
        random[ObligationDetails].copy(
          status = Fulfilled
        )

      val obligationData =
        ObligationData(
          Seq(Obligation(None, Seq(openObligation)))
        )

      stubGetObligations(obligationData)

      val result = callRoute(
        FakeRequest(routes.BtaTileDataController.getBtaTileData)
      )

      val expectedBtaTileData = BtaTileData(
        eclReference = testEclReference,
        dueReturn = None
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.desEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
        )
        resetAllRequests()
      }
    }

    "return 200 OK with BtaTileData containing eclReference and None in dueReturn field " in {
      stubAuthorised()

      stubObligationsNotFound()

      val expectedBtaTileData = BtaTileData(
        eclReference = testEclReference,
        dueReturn = None
      )

      val result = callRoute(
        FakeRequest(routes.BtaTileDataController.getBtaTileData)
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(
        expectedBtaTileData
      )

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.desEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
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
      resetAllRequests()
      stubAuthorised()

      val errorMessage = "Bad Request"
      stubObligationsUpstreamError(statusCode, errorMessage)

      val result = callRoute(
        FakeRequest(routes.BtaTileDataController.getBtaTileData)
      )

      status(result)        shouldBe BAD_GATEWAY
      contentAsJson(result) shouldBe Json.toJson(
        ResponseError.badGateway(s"Get Obligation Data Failed - $errorMessage", statusCode)
      )

      eventually {
        verify(
          getRequestedFor(urlMatching(getObligationDataRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.desBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.desEnvironment))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
        )
      }
    }
  }
}
