/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.economiccrimelevyaccount.services

import cats.data.EitherT
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.http.Status.UNPROCESSABLE_ENTITY
import uk.gov.hmrc.economiccrimelevyaccount.connectors.HipConnector
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.DocumentType.Other
import uk.gov.hmrc.economiccrimelevyaccount.models.hip.{FinancialDataHIP, HipWrappedError}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class HIPService @Inject() (hipConnector: HipConnector)(implicit ec: ExecutionContext) extends Logging {

  def getFinancialDataHIP(
    eclReference: EclReference
  )(implicit hc: HeaderCarrier): EitherT[Future, HipWrappedError, Option[FinancialDataHIP]] =
    EitherT {
      (for {
        financialDataHIP <- hipConnector.getFinancialDetails(eclReference)
        filteredResult    = filterOutUnknownDocumentTypes(financialDataHIP)
        _                 =
          logger.info(
            s"Successfully retrieved and filtered financial data for ECL-HIP reference--> ${eclReference.value}"
          )
      } yield Right(Some(filteredResult))).recover {
        case UpstreamErrorResponse(_, NOT_FOUND, _, _)                  =>
          Right(None)
        case UpstreamErrorResponse(message, UNPROCESSABLE_ENTITY, _, _) =>
          val statCode = parseJsonCode(message)
          if (statCode.contains("018")) {
            logger.info(
              s"Received HTTP 422 with code '018' for ECL-HIP reference: ${eclReference.value}. Returning No Data."
            )
            Right(None)
          } else {
            logger.error(
              s"Failed to retrieve financial data with http 422 for ECL-HIP reference: ${eclReference.value}. Reason: $message, Code: 422"
            )
            Left(HipWrappedError.BadGateway(reason = s"Get Financial Data Failed - $message", code = 422))
          }
        case error @ UpstreamErrorResponse(message, code, _, _)
            if UpstreamErrorResponse.Upstream5xxResponse
              .unapply(error)
              .isDefined || UpstreamErrorResponse.Upstream4xxResponse.unapply(error).isDefined =>
          logger.error(
            s"Failed to retrieve financial data for ECL-HIP reference: ${eclReference.value}. Reason: $message, Code: $code"
          )
          Left(HipWrappedError.BadGateway(reason = s"Get Financial Data Failed - $message", code = code))
        case NonFatal(thr)                                              =>
          logger.error(
            s"An unexpected error occurred while retrieving financial data for ECL-HIP reference: ${eclReference.value}",
            thr
          )
          Left(HipWrappedError.InternalUnexpectedError(thr.getMessage, Some(thr)))
      }
    }

  def filterOutUnknownDocumentTypes(financialDataHIP: FinancialDataHIP): FinancialDataHIP = {
    val documentsWithKnownTypes = financialDataHIP.documentDetails.map(documentDetailsList =>
      documentDetailsList.filterNot(_.documentType.exists(_.isInstanceOf[Other]))
    )
    FinancialDataHIP(financialDataHIP.totalisation, documentsWithKnownTypes)
  }

  def parseJsonCode(message: String): Option[String] =
    try {
      val json = Json.parse(message)
      (json \ "errors" \ "code").asOpt[String]
    } catch {
      case NonFatal(_) => None
    }
}
