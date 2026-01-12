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

package uk.gov.hmrc.economiccrimelevyaccount.connectors
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{DocumentDetails, DocumentType, FinancialData, LineItemDetails, PenaltyTotals, Totalisation}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import scala.util.{Failure, Try}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HipConnectorSpec extends PlaySpec with MockitoSugar {
  "HipConnector" must {
    "return financial details when the http client returns financial details" in {
      val mockAppConfig                      = mock[AppConfig]
      val mockHttpClient                     = mock[HttpClientV2]
      val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
      val hipConnector                       = new HipConnector(mockAppConfig, mockHttpClient)
      when(mockAppConfig.hipUrl).thenReturn(
        "http://localhost:9099/etmp/RESTAdapter/cross-regime/taxpayer/financial-data/query"
      )
      when(mockAppConfig.hipToken).thenReturn("mockToken")
      when(mockAppConfig.hipServiceOriginatorIdKeyV1).thenReturn("mock-originator-id-key")
      when(mockAppConfig.hipServiceOriginatorIdV1).thenReturn("mock-originator-id")
      when(mockAppConfig.hipDateFrom).thenReturn(LocalDate.of(2023, 1, 1))
      val mockFinancialData                  = createMockFinancialDataHIP()
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(
        Future.successful(
          HttpResponse.apply(OK, Json.stringify(Json.toJson(mockFinancialData)))
        )
      )
      implicit val hc: HeaderCarrier         = HeaderCarrier()
      val eclReference                       = EclReference("ECL1234")
      await(hipConnector.getFinancialDetails(eclReference)).isInstanceOf[FinancialData] shouldBe true
    }

    "when a 500x error is returned from HIP API" in {
      val mockAppConfig                      = mock[AppConfig]
      val mockHttpClient                     = mock[HttpClientV2]
      val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
      val errorMessage                       = "internal server error"
      val eclReference                       = EclReference("ECL1234")
      val hipConnector                       = new HipConnector(mockAppConfig, mockHttpClient)
      when(mockAppConfig.hipUrl).thenReturn(
        "http://localhost:9099/etmp/RESTAdapter/cross-regime/taxpayer/financial-data/query"
      )
      when(mockAppConfig.hipToken).thenReturn("mockToken")
      when(mockAppConfig.hipServiceOriginatorIdKeyV1).thenReturn("mock-originator-id-key")
      when(mockAppConfig.hipServiceOriginatorIdV1).thenReturn("mock-originator-id")
      when(mockAppConfig.hipDateFrom).thenReturn(LocalDate.of(2023, 1, 1))
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.successful(HttpResponse.apply(INTERNAL_SERVER_ERROR, errorMessage)))
      implicit val hc: HeaderCarrier         = HeaderCarrier()
      Try(await(hipConnector.getFinancialDetails(eclReference))) match {
        case Failure(UpstreamErrorResponse(msg, _, _, _)) =>
          msg shouldEqual errorMessage
        case _                                            =>
          fail("expected UpstreamErrorResponse when an error is received from HIP")
      }
    }
  }
  private def createMockFinancialDataHIP(): FinancialData = {
    val totalisation = Totalisation(
      totalAccountBalance = Some(BigDecimal(1250)),
      totalAccountOverdue = Some(BigDecimal(1000)),
      totalOverdue = Some(BigDecimal(100)),
      totalNotYetDue = Some(BigDecimal(250)),
      totalBalance = Some(BigDecimal(100)),
      totalCredit = Some(BigDecimal(0)),
      totalCleared = Some(BigDecimal(50))
    )

    val lineItemDetails = Seq(
      LineItemDetails(
        chargeDescription = Some("ECL Return"),
        periodFromDate = Some("2022-01-01"),
        periodToDate = Some("2022-01-31"),
        periodKey = Some("22YD"),
        netDueDate = Some("2022-02-08"),
        amount = Some(BigDecimal(3420)),
        clearingDate = Some("2022-02-09"),
        clearingReason = Some("01"),
        clearingDocument = Some("719283701921"),
        mainTransaction = Some("6220"),
        subTransaction = Some("a")
      )
    )

    val penaltyTotals = Seq(
      PenaltyTotals(
        penaltyType = Some("LPP1"),
        penaltyStatus = Some("POSTED"),
        penaltyAmount = Some(BigDecimal(10.01)),
        postedChargeReference = Some("XR00123933492")
      )
    )

    val documentDetails = Seq(
      DocumentDetails(
        documentType = Some(DocumentType.NewCharge),
        chargeReferenceNumber = Some("XP001286394838"),
        postingDate = Some("2022-01-01"),
        issueDate = Some("2022-01-01"),
        documentTotalAmount = Some(BigDecimal(100)),
        documentClearedAmount = Some(BigDecimal(100)),
        documentOutstandingAmount = Some(BigDecimal(0)),
        lineItemDetails = Some(lineItemDetails),
        interestPostedAmount = Some(BigDecimal(13.12)),
        interestAccruingAmount = Some(BigDecimal(12.1)),
        interestPostedChargeRef = Some("XB001286323438"),
        penaltyTotals = Some(penaltyTotals),
        contractObjectNumber = Some("104920928302302"),
        contractObjectType = Some("ECL")
      )
    )

    FinancialData(
      totalisation = Some(totalisation),
      documentDetails = Some(documentDetails)
    )
  }
}
