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

package uk.gov.hmrc.economiccrimelevyaccount.generators

import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.economiccrimelevyaccount.EclTestData
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Fulfilled, Identification, Obligation, ObligationData, ObligationDetails, ObligationStatus, Open}
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{DocumentDetails, DocumentType, FinancialData, LineItemDetails, PenaltyTotals, Totalisation}

import java.time.LocalDate

object CachedArbitraries extends EclTestData with Generators {

  private val genNonEmptyString: Gen[String] =
    Gen.alphaNumStr.suchThat(_.nonEmpty)

  private val genBigDecimal: Gen[BigDecimal] =
    Gen.chooseNum(-1000000L, 1000000L).map(BigDecimal(_))

  implicit lazy val arbLocalDate: Arbitrary[LocalDate] =
    Arbitrary(
      Gen.chooseNum(0L, 3652058L).map(LocalDate.ofEpochDay)
    )

  implicit lazy val arbIdentification: Arbitrary[Identification] =
    Arbitrary(
      for {
        incomeSourceType <- Gen.option(genNonEmptyString)
        referenceNumber  <- genNonEmptyString
        referenceType    <- genNonEmptyString
      } yield Identification(
        incomeSourceType = incomeSourceType,
        referenceNumber = referenceNumber,
        referenceType = referenceType
      )
    )

  override implicit lazy val arbEnrolmentIdentifier: Arbitrary[EnrolmentIdentifier] =
    Arbitrary(
      for {
        key   <- genNonEmptyString
        value <- genNonEmptyString
      } yield EnrolmentIdentifier(
        key = key,
        value = value
      )
    )

  override implicit lazy val arbEnrolment: Arbitrary[Enrolment] =
    Arbitrary(
      for {
        key               <- genNonEmptyString
        identifiers       <- Gen.listOf(arbEnrolmentIdentifier.arbitrary).map(_.toSeq)
        state             <- genNonEmptyString
        delegatedAuthRule <- Gen.option(genNonEmptyString)
      } yield Enrolment(
        key = key,
        identifiers = identifiers,
        state = state,
        delegatedAuthRule = delegatedAuthRule
      )
    )

  implicit lazy val arbDocumentType: Arbitrary[DocumentType] =
    Arbitrary(
      Gen.oneOf(
        Gen.const(DocumentType.NewCharge),
        Gen.const(DocumentType.AmendedCharge),
        Gen.const(DocumentType.InterestCharge),
        Gen.const(DocumentType.Payment),
        Gen.alphaNumStr.suchThat(_.nonEmpty).map(DocumentType.Other.apply)
      )
    )

  implicit lazy val arbFinancialDataResponse: Arbitrary[FinancialData] =
    Arbitrary(
      for {
        totalisation    <- Gen.option(
                             for {
                               totalAccountBalance <- Gen.option(genBigDecimal)
                               totalAccountOverdue <- Gen.option(genBigDecimal)
                               totalOverdue        <- Gen.option(genBigDecimal)
                               totalNotYetDue      <- Gen.option(genBigDecimal)
                               totalBalance        <- Gen.option(genBigDecimal)
                               totalCredit         <- Gen.option(genBigDecimal)
                               totalCleared        <- Gen.option(genBigDecimal)
                             } yield Totalisation(
                               totalAccountBalance = totalAccountBalance,
                               totalAccountOverdue = totalAccountOverdue,
                               totalOverdue = totalOverdue,
                               totalNotYetDue = totalNotYetDue,
                               totalBalance = totalBalance,
                               totalCredit = totalCredit,
                               totalCleared = totalCleared
                             )
                           )
        documentDetails <- Gen.option(
                             Gen
                               .listOf(
                                 for {
                                   documentType              <- Gen.option(Arbitrary.arbitrary[DocumentType])
                                   chargeReferenceNumber     <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                   postingDate               <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                   issueDate                 <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                   documentTotalAmount       <- Gen.option(genBigDecimal)
                                   documentClearedAmount     <- Gen.option(genBigDecimal)
                                   documentOutstandingAmount <- Gen.option(genBigDecimal)
                                   lineItemDetails           <-
                                     Gen.option(
                                       Gen
                                         .listOf(
                                           for {
                                             chargeDescription <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                             periodFromDate    <- Gen.option(Gen.alphaNumStr)
                                             periodToDate      <- Gen.option(Gen.alphaNumStr)
                                             periodKey         <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                             netDueDate        <- Gen.option(Gen.alphaNumStr)
                                             amount            <- Gen.option(Gen.chooseNum(-1000000L, 1000000L).map(BigDecimal(_)))
                                             clearingDate      <- Gen.option(Gen.alphaNumStr)
                                             clearingReason    <- Gen.option(Gen.alphaNumStr)
                                             clearingDocument  <- Gen.option(Gen.alphaNumStr)
                                             mainTransaction   <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                             subTransaction    <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                           } yield LineItemDetails(
                                             chargeDescription = chargeDescription,
                                             periodFromDate = periodFromDate,
                                             periodToDate = periodToDate,
                                             periodKey = periodKey,
                                             netDueDate = netDueDate,
                                             amount = amount,
                                             clearingDate = clearingDate,
                                             clearingReason = clearingReason,
                                             clearingDocument = clearingDocument,
                                             mainTransaction = mainTransaction,
                                             subTransaction = subTransaction
                                           )
                                         )
                                         .map(_.toSeq)
                                     )
                                   interestPostedAmount      <- Gen.option(genBigDecimal)
                                   interestAccruingAmount    <- Gen.option(genBigDecimal)
                                   interestPostedChargeRef   <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                   penaltyTotals             <-
                                     Gen.option(
                                       Gen
                                         .listOf(
                                           for {
                                             penaltyType           <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                             penaltyStatus         <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                             penaltyAmount         <-
                                               Gen.option(Gen.chooseNum(-1000000L, 1000000L).map(BigDecimal(_)))
                                             postedChargeReference <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                           } yield PenaltyTotals(
                                             penaltyType = penaltyType,
                                             penaltyStatus = penaltyStatus,
                                             penaltyAmount = penaltyAmount,
                                             postedChargeReference = postedChargeReference
                                           )
                                         )
                                         .map(_.toSeq)
                                     )
                                   contractObjectNumber      <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                   contractObjectType        <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
                                 } yield DocumentDetails(
                                   documentType = documentType,
                                   chargeReferenceNumber = chargeReferenceNumber,
                                   postingDate = postingDate,
                                   issueDate = issueDate,
                                   documentTotalAmount = documentTotalAmount,
                                   documentClearedAmount = documentClearedAmount,
                                   documentOutstandingAmount = documentOutstandingAmount,
                                   lineItemDetails = lineItemDetails,
                                   interestPostedAmount = interestPostedAmount,
                                   interestAccruingAmount = interestAccruingAmount,
                                   interestPostedChargeRef = interestPostedChargeRef,
                                   penaltyTotals = penaltyTotals,
                                   contractObjectNumber = contractObjectNumber,
                                   contractObjectType = contractObjectType
                                 )
                               )
                               .map(_.toSeq)
                           )
      } yield FinancialData(
        totalisation = totalisation,
        documentDetails = documentDetails
      )
    )

  implicit lazy val arbObligationStatus: Arbitrary[ObligationStatus] =
    Arbitrary(
      Gen.oneOf(
        Open,
        Fulfilled
      )
    )

  implicit lazy val arbObligationDetails: Arbitrary[ObligationDetails] =
    Arbitrary(
      for {
        status                            <- arbObligationStatus.arbitrary
        inboundCorrespondenceFromDate     <- arbLocalDate.arbitrary
        inboundCorrespondenceToDate       <- arbLocalDate.arbitrary
        inboundCorrespondenceDateReceived <- Gen.option(arbLocalDate.arbitrary)
        inboundCorrespondenceDueDate      <- arbLocalDate.arbitrary
        periodKey                         <- genNonEmptyString
      } yield ObligationDetails(
        status = status,
        inboundCorrespondenceFromDate = inboundCorrespondenceFromDate,
        inboundCorrespondenceToDate = inboundCorrespondenceToDate,
        inboundCorrespondenceDateReceived = inboundCorrespondenceDateReceived,
        inboundCorrespondenceDueDate = inboundCorrespondenceDueDate,
        periodKey = periodKey
      )
    )

  private lazy val genObligation: Gen[Obligation] =
    for {
      identification    <- Gen.option(arbIdentification.arbitrary)
      obligationDetails <- Gen.listOf(arbObligationDetails.arbitrary).map(_.toSeq)
    } yield Obligation(
      identification = identification,
      obligationDetails = obligationDetails
    )

  implicit lazy val arbObligation: Arbitrary[Obligation] =
    Arbitrary(genObligation)

  implicit lazy val arbObligationData: Arbitrary[ObligationData] =
    Arbitrary(
      Gen.listOf(genObligation).map(obligations => ObligationData(obligations = obligations.toSeq))
    )

  implicit lazy val arbOptObligationData: Arbitrary[Option[ObligationData]] =
    Arbitrary(
      Gen.option(arbObligationData.arbitrary)
    )
}
