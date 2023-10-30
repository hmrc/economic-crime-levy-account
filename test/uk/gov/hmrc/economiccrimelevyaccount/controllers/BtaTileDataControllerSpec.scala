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
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.bta.{BtaTileData, DueReturn}
import uk.gov.hmrc.economiccrimelevyaccount.models.des._
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.DesSubmissionError
import uk.gov.hmrc.economiccrimelevyaccount.services.DesService

import java.time.LocalDate
import scala.concurrent.Future

class BtaTileDataControllerSpec extends SpecBase {

  val mockObligationDataService: DesService = mock[DesService]

  val controller = new BtaTileDataController(
    cc,
    fakeAuthorisedAction,
    mockObligationDataService
  )

  "getBtaTileData" should {
    "return 200 OK with no due return when there is no obligation data" in forAll { obligationData: ObligationData =>
      when(mockObligationDataService.getObligationData(any())(any()))
        .thenReturn(EitherT.rightT[Future, DesSubmissionError](obligationData))

      val result: Future[Result] =
        controller.getBtaTileData()(fakeRequest)

      val expectedBtaTileData = BtaTileData(
        eclReference = EclReference("test-ecl-registration-reference"),
        dueReturn = None
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)
    }

    "return 200 OK with no due return when there are obligations due with a Fulfilled status but none with an Open status" in forAll {
      (o: ObligationDetails) =>
        val openObligation1 = o.copy(status = Fulfilled, inboundCorrespondenceDueDate = LocalDate.parse("2021-09-30"))
        val openObligation2 = o.copy(status = Fulfilled, inboundCorrespondenceDueDate = LocalDate.parse("2022-09-30"))
        val openObligation3 = o.copy(status = Fulfilled, inboundCorrespondenceDueDate = LocalDate.parse("2023-09-30"))

        val obligationData =
          ObligationData(Seq(Obligation(None, Seq(openObligation1, openObligation2, openObligation3))))

        when(mockObligationDataService.getObligationData(any())(any()))
          .thenReturn(EitherT.rightT[Future, DesSubmissionError](obligationData))

        val result: Future[Result] =
          controller.getBtaTileData()(fakeRequest)

        val expectedBtaTileData = BtaTileData(
          eclReference = EclReference("test-ecl-registration-reference"),
          dueReturn = None
        )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)
    }

    "return 200 OK with the highest priority return due when there are multiple obligations with Open and Fulfilled statuses" in forAll {
      (o: ObligationDetails) =>
        val fulfilledObligation       =
          o.copy(status = Fulfilled, inboundCorrespondenceDueDate = LocalDate.parse("2021-09-30"))
        val highestPriorityObligation =
          o.copy(status = Open, inboundCorrespondenceDueDate = LocalDate.parse("2022-09-30"))
        val otherOpenObligation       = o.copy(status = Open, inboundCorrespondenceDueDate = LocalDate.parse("2023-09-30"))

        val obligationData =
          ObligationData(
            Seq(Obligation(None, Seq(fulfilledObligation, highestPriorityObligation, otherOpenObligation)))
          )

        when(mockObligationDataService.getObligationData(any())(any()))
          .thenReturn(EitherT.rightT[Future, DesSubmissionError](obligationData))

        val result: Future[Result] =
          controller.getBtaTileData()(fakeRequest)

        val expectedBtaTileData = BtaTileData(
          eclReference = EclReference("test-ecl-registration-reference"),
          dueReturn = Some(
            DueReturn(
              isOverdue = true,
              dueDate = highestPriorityObligation.inboundCorrespondenceDueDate,
              periodStartDate = highestPriorityObligation.inboundCorrespondenceFromDate,
              periodEndDate = highestPriorityObligation.inboundCorrespondenceToDate,
              fyStartYear = highestPriorityObligation.inboundCorrespondenceFromDate.getYear.toString,
              fyEndYear = highestPriorityObligation.inboundCorrespondenceToDate.getYear.toString
            )
          )
        )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)
    }
  }

}
