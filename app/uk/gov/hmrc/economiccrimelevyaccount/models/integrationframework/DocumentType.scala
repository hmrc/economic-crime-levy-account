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

package uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

sealed trait DocumentType

object DocumentType {
  case object NewCharge extends DocumentType

  case object AmendedCharge extends DocumentType

  case object InterestCharge extends DocumentType

  case object Payment extends DocumentType

  case class Other(value: String) extends DocumentType

  implicit val format: Format[DocumentType] = new Format[DocumentType] {
    override def reads(json: JsValue): JsResult[DocumentType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "TRM New Charge"    => JsSuccess(NewCharge)
          case "TRM Amend Charge"  => JsSuccess(AmendedCharge)
          case "Interest Document" => JsSuccess(InterestCharge)
          case "Payment"           => JsSuccess(Payment)
          case value               => JsSuccess(Other(value))
        }
      case e: JsError          => e
    }

    override def writes(o: DocumentType): JsValue = o match {
      case NewCharge      => JsString("TRM New Charge")
      case AmendedCharge  => JsString("TRM Amend Charge")
      case InterestCharge => JsString("Interest Document")
      case Payment        => JsString("Payment")
      case Other(value)   => JsString(value)
    }
  }
}
