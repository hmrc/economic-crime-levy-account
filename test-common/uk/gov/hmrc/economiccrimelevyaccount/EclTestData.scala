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
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{DocumentDetails, DocumentType, FinancialData}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{LineItemDetails, PenaltyTotals, Totalisation}

trait EclTestData {

  val uuidRegex: String = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  def arbEnrolments(withEcl: Boolean): Arbitrary[Enrolments] = Arbitrary {
    for {
      enrolments               <- Gen.containerOf[Set, Enrolment](Arbitrary.arbitrary[Enrolment])
      enrolment                <- Arbitrary.arbitrary[Enrolment]
      eclRegistrationReference <- Arbitrary.arbitrary[String]
      eclEnrolmentIdentifier    = EnrolmentIdentifier(EclEnrolment.IdentifierKey, eclRegistrationReference)
      eclEnrolment              =
        enrolment.copy(key = EclEnrolment.ServiceName, identifiers = enrolment.identifiers :+ eclEnrolmentIdentifier)
    } yield if (withEcl) Enrolments(enrolments + eclEnrolment) else Enrolments(enrolments)
  }

  implicit val arbBigDecimal: Arbitrary[BigDecimal] = Arbitrary {
    for {
      value <- Arbitrary.arbitrary[Int]
    } yield BigDecimal(value)
  }

  implicit val arbTotalisation: Arbitrary[Totalisation] = Arbitrary {
    for {
      totalAccountBalance <- Arbitrary.arbitrary[BigDecimal]
      totalAccountOverdue <- Arbitrary.arbitrary[BigDecimal]
      totalOverdue        <- Arbitrary.arbitrary[BigDecimal]
      totalNotYetDue      <- Arbitrary.arbitrary[BigDecimal]
      totalBalance        <- Arbitrary.arbitrary[BigDecimal]
      totalCredit         <- Arbitrary.arbitrary[BigDecimal]
      totalCleared        <- Arbitrary.arbitrary[BigDecimal]
    } yield Totalisation(
      Some(totalAccountBalance),
      Some(totalAccountOverdue),
      Some(totalOverdue),
      Some(totalNotYetDue),
      Some(totalBalance),
      Some(totalCredit),
      Some(totalCleared)
    )
  }

  implicit val arbDocumentTypes: Arbitrary[DocumentType with Serializable] = Arbitrary {
    Gen.oneOf(
      Seq(
        DocumentType.InterestCharge,
        DocumentType.NewCharge,
        DocumentType.Payment,
        DocumentType.AmendedCharge
      )
    )
  }

  implicit val arbDocumentDetails: Arbitrary[DocumentDetails] = Arbitrary {
    for {
      documentType              <- arbDocumentTypes.arbitrary.map(Some(_))
      chargeReferenceNumber     <- Arbitrary.arbitrary[String].map(Some(_))
      postingDate               <- Arbitrary.arbitrary[String].map(Some(_))
      issueDate                 <- Arbitrary.arbitrary[String].map(Some(_))
      documentTotalAmount       <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      documentClearedAmount     <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      documentOutstandingAmount <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      lineItemDetails           <- Arbitrary.arbitrary[Seq[LineItemDetails]].map(Some(_))
      interestPostedAmount      <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      interestAccruingAmount    <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      interestPostedChargeRef   <- Arbitrary.arbitrary[String].map(Some(_))
      penaltyTotals             <- Arbitrary.arbitrary[Seq[PenaltyTotals]].map(Some(_))
      contractObjectNumber      <- Arbitrary.arbitrary[String].map(Some(_))
      contractObjectType        <- Arbitrary.arbitrary[String].map(Some(_))
    } yield DocumentDetails(
      documentType,
      chargeReferenceNumber,
      postingDate,
      issueDate,
      documentTotalAmount,
      documentClearedAmount,
      documentOutstandingAmount,
      lineItemDetails,
      interestPostedAmount,
      interestAccruingAmount,
      interestPostedChargeRef,
      penaltyTotals,
      contractObjectNumber,
      contractObjectType
    )
  }

  implicit val arbValidFinancialDataResponse: Arbitrary[FinancialData] = Arbitrary {
    for {
      totalisation    <- Arbitrary.arbitrary[Totalisation].map(Some(_))
      documentDetails <- Gen.nonEmptyContainerOf[Seq, DocumentDetails](Arbitrary.arbitrary[DocumentDetails])
    } yield FinancialData(
      totalisation,
      Some(documentDetails)
    )
  }

  implicit val arbEclReference: Arbitrary[EclReference] = Arbitrary(Gen.alphaNumStr.map(EclReference(_)))

  val testInternalId: String = Gen.alphaNumStr.sample.get

  val testEclReference: EclReference = arbEclReference.arbitrary.sample.get
}
