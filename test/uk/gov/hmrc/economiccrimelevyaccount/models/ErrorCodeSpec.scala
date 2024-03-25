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

package uk.gov.hmrc.economiccrimelevyaccount.models

import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ErrorCode

class ErrorCodeSpec extends SpecBase {

  "reads" should {
    ErrorCode.errorCodes.foreach { errorCode =>
      s"return the error code ${errorCode.statusCode} deserialized from its JSON representation" in {
        val json = Json.toJson(errorCode)

        json.as[ErrorCode] shouldBe errorCode
      }
    }

    "return a JsError when passed an invalid string value" in {
      val result = Json.fromJson[ErrorCode](JsString("Test"))

      result shouldBe a[JsError]
    }
  }
}
