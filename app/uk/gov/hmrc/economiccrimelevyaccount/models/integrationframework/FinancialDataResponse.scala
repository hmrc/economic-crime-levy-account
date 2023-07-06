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

import play.api.http.Status._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
case class FinancialDataResponse(totalisation: Option[Totalisation], documentDetails: Option[Seq[DocumentDetails]])

object FinancialDataResponse {

  implicit object FinancialDataResponseReads
      extends HttpReads[Either[FinancialDataErrorResponse, FinancialDataResponse]] {
    override def read(
      method: String,
      url: String,
      response: HttpResponse
    ): Either[FinancialDataErrorResponse, FinancialDataResponse] = {
      val validationResult = response.json.validate[FinancialDataErrorResponse]
      response.status match {
        case OK                    =>
          response.json.validate[FinancialDataResponse] match {
            case JsSuccess(response, _) => Right(response)
          }
        case BAD_REQUEST           =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
        case NOT_FOUND             =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
        case CONFLICT              =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
        case UNPROCESSABLE_ENTITY  =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
        case INTERNAL_SERVER_ERROR =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
        case SERVICE_UNAVAILABLE   =>
          validationResult match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
          }
      }
    }
  }
  implicit val reads: Reads[FinancialDataResponse] = (
    (JsPath \ "getFinancialData" \ "financialDetails" \ "totalisation").readNullable[Totalisation] and
      (JsPath \ "getFinancialData" \ "financialDetails" \ "documentDetails").readNullable[Seq[DocumentDetails]]
  )(FinancialDataResponse.apply _)

  implicit val writes: OWrites[FinancialDataResponse] = Json.writes[FinancialDataResponse]
}

case class Totalisation(
  totalOverdue: Option[BigDecimal],
  totalNotYetDue: Option[BigDecimal],
  totalBalance: Option[BigDecimal],
  totalCredit: Option[BigDecimal],
  totalCleared: Option[BigDecimal]
)

object Totalisation {
  implicit val reads: Reads[Totalisation] = (
    (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalOverdue").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalNotYetDue").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalBalance").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalCredit").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalCleared").readNullable[BigDecimal]
  )(Totalisation.apply _)

  implicit val writes: OWrites[Totalisation] = Json.writes[Totalisation]
}

case class DocumentDetails(
  documentType: Option[FinancialDataDocumentType],
  chargeReferenceNumber: Option[String],
  postingDate: Option[String],
  issueDate: Option[String],
  documentTotalAmount: Option[BigDecimal],
  documentClearedAmount: Option[BigDecimal],
  documentOutstandingAmount: Option[BigDecimal],
  lineItemDetails: Option[Seq[LineItemDetails]]
)
object DocumentDetails {

  implicit val reads: Reads[DocumentDetails] = (
    (JsPath \ "documentType").readNullable[FinancialDataDocumentType] and
      (JsPath \ "chargeReferenceNumber").readNullable[String] and
      (JsPath \ "postingDate").readNullable[String] and
      (JsPath \ "issueDate").readNullable[String] and
      (JsPath \ "documentTotalAmount").readNullable[BigDecimal] and
      (JsPath \ "documentClearedAmount").readNullable[BigDecimal] and
      (JsPath \ "documentOutstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "lineItemDetails").readNullable[Seq[LineItemDetails]]
  )(DocumentDetails.apply _)

  implicit var writes: OWrites[DocumentDetails] = Json.writes[DocumentDetails]
}

sealed trait FinancialDataDocumentType

case object NewCharge extends FinancialDataDocumentType

case object AmendedCharge extends FinancialDataDocumentType

case object ReversedCharge extends FinancialDataDocumentType

object FinancialDataDocumentType {
  implicit val format: Format[FinancialDataDocumentType] = new Format[FinancialDataDocumentType] {
    override def reads(json: JsValue): JsResult[FinancialDataDocumentType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "TRM New Charge"      => JsSuccess(NewCharge)
          case "TRM Amended Charge"  => JsSuccess(AmendedCharge)
          case "TRM Reversed Charge" => JsSuccess(ReversedCharge)
          case _                     => JsError("Invalid charge type has been passed")
        }
      case e: JsError          => e
    }

    override def writes(o: FinancialDataDocumentType): JsValue = o match {
      case NewCharge      => JsString("TRM New Charge")
      case AmendedCharge  => JsString("TRM Amended Charge")
      case ReversedCharge => JsString("TRM Reversed Charge")
    }
  }
}

case class LineItemDetails(
  chargeDescription: Option[String],
  periodFromDate: Option[String],
  periodToDate: Option[String],
  periodKey: Option[String],
  netDueDate: Option[String],
  amount: Option[BigDecimal],
  clearingDate: Option[String]
)

object LineItemDetails {
  implicit val format: OFormat[LineItemDetails] = Json.format[LineItemDetails]
}
