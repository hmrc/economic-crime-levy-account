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

package uk.gov.hmrc.economiccrimelevyaccount

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.derivedArbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{DocumentDetails, FinancialDataResponse, LineItemDetails, Totalisation}

import java.time.{Instant, LocalDate}

case class EnrolmentsWithEcl(enrolments: Enrolments)

case class EnrolmentsWithoutEcl(enrolments: Enrolments)

case class ValidFinancialDataResponse(financialDataResponse: FinancialDataResponse)

trait EclTestData {

  private val currentYear       = LocalDate.now().getYear
  private val startDayFY: Int   = 1
  private val endDayFY: Int     = 31
  private val startMonthFY: Int = 4
  private val endMonthFY: Int   = 3

  implicit val arbInstant: Arbitrary[Instant] = Arbitrary {
    Instant.now()
  }

  implicit val arbLocalDate: Arbitrary[LocalDate] = Arbitrary {
    LocalDate.now()
  }

  implicit val arbEnrolmentsWithEcl: Arbitrary[EnrolmentsWithEcl] = Arbitrary {
    for {
      enrolments               <- Arbitrary.arbitrary[Enrolments]
      enrolment                <- Arbitrary.arbitrary[Enrolment]
      eclRegistrationReference <- Arbitrary.arbitrary[String]
      eclEnrolmentIdentifier    = EnrolmentIdentifier(EclEnrolment.IdentifierKey, eclRegistrationReference)
      eclEnrolment              =
        enrolment.copy(key = EclEnrolment.ServiceName, identifiers = enrolment.identifiers :+ eclEnrolmentIdentifier)
    } yield EnrolmentsWithEcl(enrolments.copy(enrolments.enrolments + eclEnrolment))
  }

  implicit val arbValidFinancialDataResponse: Arbitrary[ValidFinancialDataResponse] = Arbitrary {
    for {
      totalisation    <- Arbitrary.arbitrary[Totalisation]
      postingDateArb  <- Arbitrary.arbitrary[LocalDate]
      issueDateArb    <- Arbitrary.arbitrary[LocalDate]
      totalAmount     <- Arbitrary.arbitrary[Int]
      clearedAmount   <- Arbitrary.arbitrary[Int]
      documentDetails <- Arbitrary.arbitrary[DocumentDetails]
      lineItemDetails <- Arbitrary.arbitrary[LineItemDetails]
      itemNetDueDate   = Arbitrary.arbitrary[LocalDate]

    } yield ValidFinancialDataResponse(
      FinancialDataResponse(
        totalisation = Some(totalisation),
        documentDetails = Some(
          Seq(
            documentDetails.copy(
              documentType = Some("TRM New Charge"),
              chargeReferenceNumber = Some("XMECL0000000001"),
              postingDate = Some(postingDateArb.toString),
              issueDate = Some(issueDateArb.toString),
              documentTotalAmount = Some(BigDecimal(totalAmount.toString)),
              documentClearedAmount = Some(BigDecimal(clearedAmount.toString)),
              documentOutstandingAmount = Some(BigDecimal(totalAmount.toString) - BigDecimal(clearedAmount.toString)),
              lineItemDetails = Some(
                Seq(
                  lineItemDetails.copy(
                    chargeDescription = Some("XMECL0000000001"),
                    periodFromDate = Some(postingDateArb.toString),
                    periodToDate = Some(postingDateArb.toString),
                    periodKey = Some(calculatePeriodKey(postingDateArb.toString.takeRight(4))),
                    netDueDate = Some(itemNetDueDate.toString),
                    amount = Some(BigDecimal(clearedAmount.toString))
                  )
                )
              )
            )
          )
        )
      )
    )
  }
  implicit val arbEnrolmentsWithoutEcl: Arbitrary[EnrolmentsWithoutEcl]             = Arbitrary {
    Arbitrary
      .arbitrary[Enrolments]
      .retryUntil(
        !_.enrolments.exists(e =>
          e.key == EclEnrolment.ServiceName && e.identifiers.exists(_.key == EclEnrolment.IdentifierKey)
        )
      )
      .map(EnrolmentsWithoutEcl)
  }

  def alphaNumericString: String = Gen.alphaNumStr.retryUntil(_.nonEmpty).sample.get

  private def calculatePeriodKey(year: String): String = s"${year.takeRight(2)}XY"

  val testInternalId: String               = alphaNumericString
  val testEclRegistrationReference: String = alphaNumericString
}
