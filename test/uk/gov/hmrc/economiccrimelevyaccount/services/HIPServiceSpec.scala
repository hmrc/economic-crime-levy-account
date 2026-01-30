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
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.connectors.HipConnector
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentType.NewCharge
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{DocumentDetails, DocumentType, FinancialData, HipWrappedError, LineItemDetails, Totalisation}
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.LocalDate
import scala.concurrent.Future

class HIPServiceSpec extends SpecBase {

  val mockHIPConnector: HipConnector = mock[HipConnector]
  val mockAppConfig: AppConfig       = mock[AppConfig]
  val hipService                     = new HIPService(mockHIPConnector, mockAppConfig)
  when(mockAppConfig.hipDateFrom).thenReturn(LocalDate.of(2023, 1, 1))
  when(mockAppConfig.batchSize).thenReturn(999)

  override def beforeEach(): Unit =
    reset(mockHIPConnector)

  "getFinancialData" should {
    "returns financial data in a defined option when the data is successfully returned from HIP connector and filters out unknown document types" in {

      val eclRef         = EclReference("ECL1234567890")
      val financialData1 = mockFinData()
      val financialData2 = mockFinData()
      when(
        mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
      )
        .thenReturn(Future.successful(financialData1), Future.successful(financialData2))

      val result = hipService.getFinancialDataHIP(eclRef).value.futureValue

      result shouldBe Right(
        Some(
          FinancialData(
            Some(Totalisation(Some(1250), Some(1000), Some(200), Some(500), Some(200), Some(0), Some(0))),
            Some(
              Vector(
                DocumentDetails(
                  Some(NewCharge),
                  Some("XMECL0000000003"),
                  None,
                  Some("2024-04-01"),
                  Some(10000),
                  Some(0),
                  Some(10000),
                  Some(
                    List(
                      LineItemDetails(
                        None,
                        None,
                        None,
                        None,
                        None,
                        Some(0),
                        None,
                        None,
                        None,
                        Some("6220"),
                        Some("3410")
                      )
                    )
                  ),
                  Some(13.12),
                  Some(12.1),
                  Some("XB001286323438"),
                  None,
                  Some("104920928302302"),
                  Some("ECL")
                ),
                DocumentDetails(
                  Some(NewCharge),
                  Some("XMECL0000000003"),
                  None,
                  Some("2024-04-01"),
                  Some(10000),
                  Some(0),
                  Some(10000),
                  Some(
                    List(
                      LineItemDetails(
                        None,
                        None,
                        None,
                        None,
                        None,
                        Some(0),
                        None,
                        None,
                        None,
                        Some("6220"),
                        Some("3410")
                      )
                    )
                  ),
                  Some(13.12),
                  Some(12.1),
                  Some("XB001286323438"),
                  None,
                  Some("104920928302302"),
                  Some("ECL")
                )
              )
            )
          )
        )
      )
    }

    "return an empty option if HIP responds with NOT_FOUND" in forAll { (eclReference: EclReference) =>
      when(
        mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
      )
        .thenReturn(Future.failed(UpstreamErrorResponse("not found", NOT_FOUND)))
      val result =
        await(hipService.getFinancialDataHIP(eclReference).value)
      result shouldBe Right(Some(FinancialData(None, None)))
    }

    "return an empty option if HIP responds with 422 status code and 018 code" in forAll {
      (eclReference: EclReference) =>
        when(
          mockHIPConnector.getFinancialDetails(any[String].asInstanceOf[EclReference], any[String], any[String])(any())
        )
          .thenReturn(
            Future.failed(
              UpstreamErrorResponse(
                Json
                  .parse(s"""
                                                                        {
                            |  "errors": {
                            |    "processingDate": "2025-09-17T09:30:47Z",
                            |    "code": "018",
                            |    "text": "No Data Identified"
                            |  }
                            |}""".stripMargin)
                  .toString(),
                UNPROCESSABLE_ENTITY
              )
            )
          )
        val result =
          await(hipService.getFinancialDataHIP(eclReference).value)
        result shouldBe Right(Some(FinancialData(None, None)))
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
  def mockFinData(): FinancialData = FinancialData(
    Some(
      Totalisation(
        totalAccountBalance = Some(1250),
        totalAccountOverdue = Some(1000),
        totalBalance = Some(100),
        totalCleared = Some(0),
        totalCredit = Some(0),
        totalNotYetDue = Some(250),
        totalOverdue = Some(100)
      )
    ),
    Some(
      Seq(
        DocumentDetails(
          chargeReferenceNumber = Some("XMECL0000000003"),
          contractObjectNumber = Some("104920928302302"),
          contractObjectType = Some("ECL"),
          documentClearedAmount = Some(0),
          documentOutstandingAmount = Some(10000),
          documentTotalAmount = Some(10000),
          documentType = Some(DocumentType.NewCharge),
          interestAccruingAmount = Some(12.1),
          interestPostedAmount = Some(13.12),
          interestPostedChargeRef = Some("XB001286323438"),
          issueDate = Some("2024-04-01"),
          lineItemDetails = Some(
            Seq(
              LineItemDetails(
                amount = Some(0),
                chargeDescription = None,
                clearingDate = None,
                clearingDocument = None,
                clearingReason = None,
                periodFromDate = None,
                periodKey = None,
                periodToDate = None,
                netDueDate = None,
                mainTransaction = Some("6220"),
                subTransaction = Some("3410")
              )
            )
          ),
          penaltyTotals = None,
          postingDate = None
        )
      )
    )
  )
}
