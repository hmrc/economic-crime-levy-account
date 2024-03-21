/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.errors._

class ErrorHandlerSpec extends SpecBase with ErrorHandler {

  "DesError" should {
    "return ResponseError.badGateway when DesError.NotFound is converted" in forAll { (eclReference: EclReference) =>
      val desError = DesError.NotFound(eclReference)

      val result: ResponseError = desErrorConverter.convert(desError)

      result shouldBe ResponseError.notFoundError(s"Unable to find record with id: ${eclReference.value}")
    }

    "return ResponseError.badGateway when DesError.BadGateway is converted" in forAll { (errorMessage: String) =>
      val desError = DesError.BadGateway(errorMessage, BAD_REQUEST)

      val result: ResponseError = desErrorConverter.convert(desError)

      result shouldBe ResponseError.badGateway(errorMessage, BAD_REQUEST)
    }

    "return ResponseError.internalServiceError when DesError.InternalUnexpectedError is converted" in forAll {
      (errorMessage: String) =>
        val desError = DesError.InternalUnexpectedError(errorMessage, None)

        val result: ResponseError = desErrorConverter.convert(desError)

        result shouldBe ResponseError.internalServiceError(errorMessage, cause = None)
    }
  }

  "integrationFrameworkErrorConverter" should {
    "return ResponseError.notFoundError when IntegrationFrameworkError.NotFound is converted" in forAll {
      (eclReference: EclReference) =>
        val integrationFrameworkError = IntegrationFrameworkError.NotFound(eclReference)

        val result: ResponseError = integrationFrameworkErrorConverter.convert(integrationFrameworkError)

        result shouldBe ResponseError.notFoundError(s"Unable to find record with id: ${eclReference.value}")
    }

    "return ResponseError.badGateway when IntegrationFrameworkError.BadGateway is converted" in forAll {
      (errorMessage: String) =>
        val integrationFrameworkError = IntegrationFrameworkError.BadGateway(errorMessage, BAD_GATEWAY)

        val result: ResponseError = integrationFrameworkErrorConverter.convert(integrationFrameworkError)

        result shouldBe ResponseError.badGateway(errorMessage, BAD_GATEWAY)
    }

    "return ResponseError.internalServiceError when IntegrationFrameworkError.InternalUnexpectedError is converted" in forAll {
      (errorMessage: String) =>
        val integrationFrameworkError = IntegrationFrameworkError.InternalUnexpectedError(errorMessage, None)

        val result: ResponseError = integrationFrameworkErrorConverter.convert(integrationFrameworkError)

        result shouldBe ResponseError.internalServiceError(errorMessage, cause = None)
    }
  }
}
