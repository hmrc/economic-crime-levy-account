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

package uk.gov.hmrc.economiccrimelevyaccount.controllers

import cats.data.EitherT
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.IntegrationFrameworkError
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.DocumentType.Other
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialData
import uk.gov.hmrc.economiccrimelevyaccount.services.IntegrationFrameworkService

import scala.concurrent.Future

class FinancialDataControllerSpec extends SpecBase {

  private val mockIntegrationFrameworkService = mock[IntegrationFrameworkService]

  val controller: FinancialDataController = new FinancialDataController(
    cc,
    fakeAuthorisedAction,
    mockIntegrationFrameworkService
  )

  override def beforeEach(): Unit =
    reset(mockIntegrationFrameworkService)

  "getFinancialData" should {
    "return 200 OK with the JSON payload when FinancialDataResponse is returned from service" in forAll {
      financialDataResponse: FinancialData =>
        when(mockIntegrationFrameworkService.getFinancialData(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(EitherT.rightT[Future, IntegrationFrameworkError](Some(financialDataResponse)))

        val result: Future[Result] =
          controller.getFinancialData()(fakeRequest)

        val documentsWithKnownTypes = financialDataResponse.documentDetails.map(documentDetailsList =>
          documentDetailsList.filterNot(_.documentType.exists(_.isInstanceOf[Other]))
        )

        val expectedResponse = FinancialData(financialDataResponse.totalisation, documentsWithKnownTypes)

        status(result) shouldBe OK

        contentAsJson(result) shouldBe Json.toJson(expectedResponse)
    }
  }

  "getFinancialData" should {
    "return 502 BadGateway when an error is returned from integration framework" in {
      when(mockIntegrationFrameworkService.getFinancialData(any[String].asInstanceOf[EclReference])(any()))
        .thenReturn(
          EitherT.leftT[Future, Option[FinancialData]](IntegrationFrameworkError.BadGateway("response body", NOT_FOUND))
        )

      val result: Future[Result] =
        controller.getFinancialData(fakeRequest)

      status(result) shouldBe BAD_GATEWAY
    }
  }
}
