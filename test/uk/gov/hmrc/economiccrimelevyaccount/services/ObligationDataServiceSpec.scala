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
import uk.gov.hmrc.economiccrimelevyaccount.connectors.DesConnector
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Obligation, ObligationData, ObligationDetails}

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.Future

class ObligationDataServiceSpec extends SpecBase {

  val mockDesConnector: DesConnector = mock[DesConnector]
  private val fixedPointInTime       = Instant.parse("2023-06-14T10:15:30.00Z")
  private val stubClock: Clock       = Clock.fixed(fixedPointInTime, ZoneId.systemDefault)

  val service = new ObligationDataService(
    mockDesConnector,
    stubClock
  )

  "getObligationData" should {
    "filter out any obligations that are due more than a year from the current date" in forAll {
      (obligationDetails: ObligationDetails, eclRegistrationReference: String) =>
        val obligationDataWithFutureObligations = ObligationData(
          obligations = Seq(
            Obligation(
              identification = None,
              obligationDetails = Seq(
                obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2022-09-30")),
                obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2023-09-30")),
                obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2024-09-30")),
                obligationDetails.copy(inboundCorrespondenceDueDate = LocalDate.parse("2025-09-30"))
              )
            )
          )
        )

        when(mockDesConnector.getObligationData(any())(any()))
          .thenReturn(Future.successful(Some(obligationDataWithFutureObligations)))

        val expectedObligations = ObligationData(
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

        val result: Option[ObligationData] =
          await(service.getObligationData(eclRegistrationReference))

        result shouldBe Some(expectedObligations)
    }
  }

}
