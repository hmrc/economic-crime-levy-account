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

import play.api.libs.json.{JsError, JsNumber, JsString, Json}
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentType
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentType.{AmendedCharge, InterestCharge, NewCharge, Payment}

class DocumentTypeSpec extends SpecBase {

  "reads" should {
    Seq(
      (NewCharge, "TRM New Charge"),
      (AmendedCharge, "TRM Amend Charge"),
      (InterestCharge, "Interest Document"),
      (Payment, "Payment")
    ).foreach { tuple =>
      val docType        = tuple._1
      val docDescription = tuple._2
      s"return $docType when the json string equals $docDescription" in {
        val json = Json.toJson(JsString(docDescription))

        json.as[DocumentType] shouldBe docType
      }
    }

    "return a JsError when the value is not a json string" in {
      val result = Json.fromJson[DocumentType](JsNumber(1))

      result shouldBe a[JsError]
    }
  }
}
