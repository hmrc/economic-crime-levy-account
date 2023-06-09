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

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.derivedArbitrary
import org.scalacheck.Arbitrary
import org.scalacheck.derive.MkArbitrary
import uk.gov.hmrc.economiccrimelevyaccount.EclTestData
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{ObligationData, ObligationStatus}
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{FinancialDataErrorResponse, FinancialDataResponse}
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationDetails

object CachedArbitraries extends EclTestData with Generators {

  private def mkArb[T](implicit mkArb: MkArbitrary[T]): Arbitrary[T] = MkArbitrary[T].arbitrary

  implicit lazy val arbEitherErrorResponseOrDataResponse
    : Arbitrary[Either[FinancialDataErrorResponse, FinancialDataResponse]]               = mkArb
  implicit lazy val arbFinancialDataResponse: Arbitrary[FinancialDataResponse]           = mkArb
  implicit lazy val arbFinancialDataErrorResponse: Arbitrary[FinancialDataErrorResponse] = mkArb
  implicit lazy val arbOptObligationData: Arbitrary[Option[ObligationData]]              = mkArb
  implicit lazy val arbObligationData: Arbitrary[ObligationData]                         = mkArb
  implicit lazy val arbObligationStatus: Arbitrary[ObligationStatus]                     = mkArb
  implicit lazy val arbObligationDetails: Arbitrary[ObligationDetails]                   = mkArb

}
