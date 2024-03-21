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

package uk.gov.hmrc.economiccrimelevyaccount.models

import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess, JsValue, Json}
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Fulfilled, ObligationStatus, Open}

class ObligationStatusSpec extends SpecBase {

  "reads" should {
    "return Open obligation status when the json string equals O" in {
      val json = Json.toJson(JsString("O"))

      json.as[ObligationStatus] shouldBe Open
    }

    "return Fulfilled obligation status when the json string equals F" in {
      val json = Json.toJson(JsString("F"))

      json.as[ObligationStatus] shouldBe Fulfilled
    }

    "return a JsError when passed a string that does not match O or P" in {
      val invalidString = "invalid"
      val result        = Json.fromJson[ObligationStatus](JsString(invalidString))

      result shouldBe JsError(s"$invalidString is not a valid ObligationStatus")
    }

    "return a JsError when the value is not a json string" in {
      val result = Json.fromJson[ObligationStatus](JsNumber(1))

      result shouldBe a[JsError]
    }
  }
}
