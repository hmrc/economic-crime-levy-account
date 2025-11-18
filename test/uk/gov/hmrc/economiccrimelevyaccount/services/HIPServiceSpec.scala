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
import org.mockito.Mockito.when
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.connectors.HipConnector
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{FinancialDataHIP, HipWrappedError}
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.LocalDate
import scala.concurrent.Future

class HIPServiceSpec extends SpecBase {

  val mockHIPConnector: HipConnector = mock[HipConnector]
  val mockAppConfig: AppConfig       = mock[AppConfig]
  val dateFrom                       = "2023-06-01"
  val dateTo                         = "2025-02-01"
  val hipService                     = new HIPService(mockHIPConnector, mockAppConfig)
  when(mockAppConfig.hipDateFrom).thenReturn(LocalDate.of(2023, 1, 1))

  override def beforeEach(): Unit =
    reset(mockHIPConnector)

  "getFinancialData" should {
    "returns financial data in a defined option when the data is successfully returned from HIP connector and filters out unknown document types" in forAll {
      (financialDataHIP: FinancialDataHIP, eclReference: EclReference) =>
        when(
          mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
        )
          .thenReturn(Future.successful(financialDataHIP))

        val findata                             = hipService.getFinancialDataHIP(eclReference)
        val financialDataWithKnownDocumentTypes = hipService.filterOutUnknownDocumentTypes(financialDataHIP)
        val result                              =
          await(hipService.getFinancialDataHIP(eclReference).value)
        result shouldBe Right(Some(findata))
    }

    "return an empty option if HIP responds with NOT_FOUND" in forAll { (eclReference: EclReference) =>
      when(
        mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
      )
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", NOT_FOUND)))

      val result =
        await(hipService.getFinancialDataHIP(eclReference).value)

      result shouldBe Right(None)
    }

    "return a bad gateway when HIP responds with an upstream error other than NOT_FOUND" in forAll {
      (eclReference: EclReference) =>
        val reason = "Forbidden"
        val code   = FORBIDDEN
        when(
          mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
        )
          .thenReturn(Future.failed(UpstreamErrorResponse(reason, code)))

        val result =
          await(hipService.getFinancialDataHIP(eclReference).value)

        result shouldBe Left(HipWrappedError.BadGateway(s"Get Financial Data Failed - $reason", code))
    }

  }
}
