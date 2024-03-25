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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Obligation, ObligationData, ObligationDetails}
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError

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
    }

    "return 200 OK with null in the body when the obligation data is not found" in {
      stubAuthorised()

      stubObligationsNotFound()

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(None)
    }

    "return 502 BAD_GATEWAY when DES responds with an upstream error" in {
      stubAuthorised()

      val statusCode   = BAD_REQUEST
      val errorMessage = "bad request"
      stubObligationsUpstreamError(statusCode, errorMessage)

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result)        shouldBe BAD_GATEWAY
      contentAsJson(result) shouldBe Json.toJson(
        ResponseError.badGateway(errorMessage, statusCode)
      )
    }

    "return 500 INTERNAL_SERVER_ERROR when an unexpected exception is thrown" in {
      stubAuthorised()

      stubGetObligationsUnexpectedResponse()

      val result = callRoute(
        FakeRequest(routes.ObligationDataController.getObligationData)
      )

      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
