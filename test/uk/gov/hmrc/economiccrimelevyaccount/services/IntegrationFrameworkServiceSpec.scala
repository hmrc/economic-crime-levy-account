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

package uk.gov.hmrc.economiccrimelevyaccount.services

import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.connectors.IntegrationFrameworkConnector
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.IntegrationFrameworkError
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialData
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.Future

class IntegrationFrameworkServiceSpec extends SpecBase {

  val mockIntegrationFrameworkConnector: IntegrationFrameworkConnector = mock[IntegrationFrameworkConnector]

  val service = new IntegrationFrameworkService(
    mockIntegrationFrameworkConnector
  )

  override def beforeEach(): Unit =
    reset(mockIntegrationFrameworkConnector)

  "getFinancialData" should {
    "returns financial data in a defined option when the data is successfully returned from IF connector and filters out unknown document types" in forAll {
      (financialData: FinancialData, eclReference: EclReference) =>
        when(mockIntegrationFrameworkConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(Future.successful(financialData))

        val financialDataWithKnownDocumentTypes = service.filterOutUnknownDocumentTypes(financialData)

        val result =
          await(service.getFinancialData(eclReference).value)

        result shouldBe Right(Some(financialDataWithKnownDocumentTypes))
    }

    "return an empty option if IF responds with NOT_FOUND" in forAll { (eclReference: EclReference) =>
      when(mockIntegrationFrameworkConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", NOT_FOUND)))

      val result =
        await(service.getFinancialData(eclReference).value)

      result shouldBe Right(None)
    }

    "return a bad gateway when IF responds with an upstream error other than NOT_FOUND" in forAll {
      (eclReference: EclReference) =>
        val reason = "Forbidden"
        val code   = FORBIDDEN
        when(mockIntegrationFrameworkConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(Future.failed(UpstreamErrorResponse(reason, code)))

        val result =
          await(service.getFinancialData(eclReference).value)

        result shouldBe Left(IntegrationFrameworkError.BadGateway(s"Get Financial Data Failed - $reason", code))
    }

    "return an InternalUnexpectedError if call to partnershipIdentificationFrontendConnector throws an exception" in forAll {
      (eclReference: EclReference) =>
        val exception = new NullPointerException("Null Pointer Exception")
        when(mockIntegrationFrameworkConnector.getFinancialDetails(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(Future.failed(exception))

        val result =
          await(service.getFinancialData(eclReference).value)

        result shouldBe Left(IntegrationFrameworkError.InternalUnexpectedError(exception.getMessage, Some(exception)))
    }

  }

}
