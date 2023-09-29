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
    ): Either[FinancialDataErrorResponse, FinancialDataResponse] =
      response.status match {
        case OK                                                                                                      =>
          response.json.validate[FinancialDataResponse] match {
            case JsSuccess(response, _) => Right(response)
            case JsError(errors)        =>
              Left(
                FinancialDataErrorResponse(
                  Some(INTERNAL_SERVER_ERROR.toString),
                  Some(errors.flatMap(_._2).mkString(","))
                )
              )
          }
        case BAD_REQUEST | NOT_FOUND | CONFLICT | UNPROCESSABLE_ENTITY | INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE =>
          response.json.validate[FinancialDataErrorResponse] match {
            case JsSuccess(errorResponse, _) => Left(errorResponse)
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
  totalAccountBalance: Option[BigDecimal],
  totalAccountOverdue: Option[BigDecimal],
  totalOverdue: Option[BigDecimal],
  totalNotYetDue: Option[BigDecimal],
  totalBalance: Option[BigDecimal],
  totalCredit: Option[BigDecimal],
  totalCleared: Option[BigDecimal]
)

object Totalisation {
  implicit val reads: Reads[Totalisation] = (
    (JsPath \ "regimeTotalisation" \ "totalAccountBalance").readNullable[BigDecimal] and
      (JsPath \ "regimeTotalisation" \ "totalAccountOverdue").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalOverdue").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalNotYetDue").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalBalance").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalCredit").readNullable[BigDecimal] and
      (JsPath \ "targetedSearch_SelectionCriteriaTotalisation" \ "totalCleared").readNullable[BigDecimal]
  )(Totalisation.apply _)

  implicit val writes: OWrites[Totalisation] = Json.writes[Totalisation]
}

case class DocumentDetails(
  documentType: Option[String],
  chargeReferenceNumber: Option[String],
  postingDate: Option[String],
  issueDate: Option[String],
  documentTotalAmount: Option[BigDecimal],
  documentClearedAmount: Option[BigDecimal],
  documentOutstandingAmount: Option[BigDecimal],
  lineItemDetails: Option[Seq[LineItemDetails]],
  interestPostedAmount: Option[BigDecimal],
  interestAccruingAmount: Option[BigDecimal],
  interestPostedChargeRef: Option[String],
  penaltyTotals: Option[Seq[PenaltyTotals]],
  contractObjectNumber: Option[String],
  contractObjectType: Option[String]
)
object DocumentDetails {

  implicit val reads: Reads[DocumentDetails] = (
    (JsPath \ "documentType").readNullable[String] and
      (JsPath \ "chargeReferenceNumber").readNullable[String] and
      (JsPath \ "postingDate").readNullable[String] and
      (JsPath \ "issueDate").readNullable[String] and
      (JsPath \ "documentTotalAmount").readNullable[BigDecimal] and
      (JsPath \ "documentClearedAmount").readNullable[BigDecimal] and
      (JsPath \ "documentOutstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "lineItemDetails").readNullable[Seq[LineItemDetails]] and
      (JsPath \ "documentInterestTotals" \ "interestPostedAmount").readNullable[BigDecimal] and
      (JsPath \ "documentInterestTotals" \ "interestAccruingAmount").readNullable[BigDecimal] and
      (JsPath \ "documentInterestTotals" \ "interestPostedChargeRef").readNullable[String] and
      (JsPath \ "documentPenaltyTotals").readNullable[Seq[PenaltyTotals]] and
      (JsPath \ "contractObjectNumber").readNullable[String] and
      (JsPath \ "contractObjectType").readNullable[String]
  )(DocumentDetails.apply _)

  implicit var writes: OWrites[DocumentDetails] = Json.writes[DocumentDetails]
}

case class LineItemDetails(
  chargeDescription: Option[String],
  periodFromDate: Option[String],
  periodToDate: Option[String],
  periodKey: Option[String],
  netDueDate: Option[String],
  amount: Option[BigDecimal],
  clearingDate: Option[String],
  clearingReason: Option[String],
  clearingDocument: Option[String]
)

object LineItemDetails {
  implicit val format: OFormat[LineItemDetails] = Json.format[LineItemDetails]
}

case class PenaltyTotals(
  penaltyType: Option[String],
  penaltyStatus: Option[String],
  penaltyAmount: Option[BigDecimal],
  postedChargeReference: Option[String]
)

object PenaltyTotals {
  implicit val format: OFormat[PenaltyTotals] = Json.format[PenaltyTotals]
}
