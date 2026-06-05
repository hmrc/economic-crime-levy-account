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

//import com.danielasfregola.randomdatagenerator.RandomDataGenerator.derivedArbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.eacd.EclEnrolment
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{DocumentDetails, DocumentType, FinancialData, LineItemDetails, PenaltyTotals, Totalisation}

trait EclTestData {

  val uuidRegex: String = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"

  private val genNonEmptyString: Gen[String] =
    Gen.alphaNumStr.suchThat(_.nonEmpty)

  implicit lazy val arbEnrolmentIdentifier: Arbitrary[EnrolmentIdentifier] =
    Arbitrary(
      for {
        key   <- genNonEmptyString
        value <- genNonEmptyString
      } yield EnrolmentIdentifier(
        key = key,
        value = value
      )
    )

  implicit lazy val arbEnrolment: Arbitrary[Enrolment] =
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

  def arbEnrolments(withEcl: Boolean): Arbitrary[Enrolments] = Arbitrary {
    for {
      enrolments               <- Gen.containerOf[Set, Enrolment](Arbitrary.arbitrary[Enrolment])
      enrolment                <- Arbitrary.arbitrary[Enrolment]
      eclRegistrationReference <- Arbitrary.arbitrary[String]
      eclEnrolmentIdentifier    = EnrolmentIdentifier(EclEnrolment.identifierKey, eclRegistrationReference)
      eclEnrolment              =
        enrolment.copy(key = EclEnrolment.serviceName, identifiers = enrolment.identifiers :+ eclEnrolmentIdentifier)
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

  implicit val arbDocumentTypes: Arbitrary[DocumentType] = Arbitrary {
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
      interestPostedAmount      <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      interestAccruingAmount    <- Arbitrary.arbitrary[BigDecimal].map(Some(_))
      interestPostedChargeRef   <- Arbitrary.arbitrary[String].map(Some(_))
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

  implicit val arbValidFinancialDataHIPResponse: Arbitrary[FinancialData] = Arbitrary {
    for {
      totalisation    <- Arbitrary.arbitrary[uk.gov.hmrc.economiccrimelevyaccount.models.hip.Totalisation].map(Some(_))
      documentDetails <- Gen.nonEmptyContainerOf[Seq, uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentDetails](
                           Arbitrary.arbitrary[uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentDetails]
                         )
    } yield FinancialData(
      totalisation,
      Some(documentDetails)
    )
  }

  implicit val arbEclReference: Arbitrary[EclReference] = Arbitrary(Gen.alphaNumStr.map(EclReference(_)))

  val testInternalId: String = Gen.alphaNumStr.sample.get

  val testEclReference: EclReference = arbEclReference.arbitrary.sample.get
}
