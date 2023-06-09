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
import uk.gov.hmrc.economiccrimelevyaccount.models.bta.{BtaTileData, DueReturn}
import uk.gov.hmrc.economiccrimelevyaccount.models.des._

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
        eclRegistrationReference = testEclRegistrationReference,
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
    }

    "return 200 OK with no due return when there is no obligation data" in {
      stubAuthorised()

      stubObligationsNotFound()

      val result = callRoute(
        FakeRequest(routes.BtaTileDataController.getBtaTileData)
      )

      val expectedBtaTileData = BtaTileData(
        eclRegistrationReference = testEclRegistrationReference,
        dueReturn = None
      )

      status(result)        shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(expectedBtaTileData)
    }
  }

}
