/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyaccount.services
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.connectors.HipConnector
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{FinancialDataHIP, HipWrappedError}
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Future

class HIPServiceSpec extends SpecBase {

  val mockHIPConnector: HipConnector = mock[HipConnector]

  val hipService = new HIPService(mockHIPConnector)

  override def beforeEach(): Unit =
    reset(mockHIPConnector)

  "getFinancialData" should {
    "returns financial data in a defined option when the data is successfully returned from HIP connector and filters out unknown document types" in forAll {
      (financialDataHIP: FinancialDataHIP, eclReference: EclReference) =>
        when(mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(Future.successful(financialDataHIP))

        val financialDataWithKnownDocumentTypes = hipService.filterOutUnknownDocumentTypes(financialDataHIP)
        val result                              =
          await(hipService.getFinancialDataHIP(eclReference).value)
        result shouldBe Right(Some(financialDataWithKnownDocumentTypes))
    }

    "return a bad gateway when IF responds with an upstream error other than NOT_FOUND" in forAll {
      (eclReference: EclReference) =>
        val reason = "Forbidden"
        val code   = FORBIDDEN
        when(mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(Future.failed(UpstreamErrorResponse(reason, code)))

        val result =
          await(hipService.getFinancialDataHIP(eclReference).value)
        result shouldBe Left(HipWrappedError.BadGateway(s"Get Financial Data Failed - $reason", code))
    }

  }
}
