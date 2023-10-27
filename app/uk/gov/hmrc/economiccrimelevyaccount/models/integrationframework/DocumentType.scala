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
