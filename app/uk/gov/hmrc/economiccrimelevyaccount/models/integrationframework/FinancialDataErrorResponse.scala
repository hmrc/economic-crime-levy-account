package uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json._
case class FinancialDataErrorResponse(errorCode: Option[String], reason: Option[String])

object FinancialDataErrorResponse {

  implicit val reads: Reads[FinancialDataErrorResponse] = (
    (JsPath \ "failures" \ "code").readNullable[String] and
      (JsPath \ "failures" \ "reason").readNullable[String]
  )(FinancialDataErrorResponse.apply _)

  implicit val writes: OWrites[FinancialDataErrorResponse] = Json.format[FinancialDataErrorResponse]
}
