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
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentType.Other
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{FinancialData, HipWrappedError}
import uk.gov.hmrc.economiccrimelevyaccount.services.HIPService

import scala.concurrent.Future

class FinancialDataControllerSpec extends SpecBase {

  private val mockHIPService = mock[HIPService]

  val controller: FinancialDataController = new FinancialDataController(
    cc,
    fakeAuthorisedAction,
    mockHIPService
  )

  override def beforeEach(): Unit =
    "getFinancialData" should {
      "return 201 OK with the JSON payload when FinancialDataResponseHIP is returned from service" in {
        financialDataResponse: FinancialData =>
          when(mockHIPService.getFinancialDataHIP(any[String].asInstanceOf[EclReference])(any()))
            .thenReturn(EitherT.rightT[Future, HipWrappedError](Some(financialDataResponse)))

          val result: Future[Result] =
            controller.getFinancialData()(fakeRequest)

          val documentsWithKnownTypes = financialDataResponse.documentDetails.map(documentDetailsList =>
            documentDetailsList.filterNot(_.documentType.exists(_.isInstanceOf[Other]))
          )

          val expectedResponse = FinancialData(financialDataResponse.totalisation, documentsWithKnownTypes)
          status(result) shouldBe OK

          contentAsJson(result) shouldBe Json.toJson(expectedResponse)
      }

      "return 502 BadGateway when an error is returned from HIP API" in {
        when(mockHIPService.getFinancialDataHIP(any[String].asInstanceOf[EclReference])(any())).thenReturn(
          EitherT.leftT[Future, Option[FinancialData]](HipWrappedError.BadGateway("response body", NOT_FOUND))
        )

        val result: Future[Result] =
          controller.getFinancialData(fakeRequest)

        status(result) shouldBe BAD_GATEWAY
      }
    }
}
