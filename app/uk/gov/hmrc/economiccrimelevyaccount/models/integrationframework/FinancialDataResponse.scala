package uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json._
case class FinancialDataResponse(totalisation: Option[Totalisation], documentDetails: Option[Seq[DocumentDetails]])

object FinancialDataResponse {

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
