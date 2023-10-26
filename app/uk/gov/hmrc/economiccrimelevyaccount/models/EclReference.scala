package uk.gov.hmrc.economiccrimelevyaccount.models

import play.api.libs.json.Json

case class EclReference(value: String) extends AnyVal

object EclReference {
  implicit val format = Json.valueFormat[EclReference]
}
