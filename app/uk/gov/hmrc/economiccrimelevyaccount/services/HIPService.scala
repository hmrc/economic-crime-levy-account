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
import shapeless.syntax.std.tuple.productTupleOps
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordering.Implicits.infixOrderingOps
import scala.util.control.NonFatal

class HIPService @Inject() (hipConnector: HipConnector, appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends Logging {

  def getFinancialDataHIP(
    eclReference: EclReference
  )(implicit hc: HeaderCarrier): EitherT[Future, HipWrappedError, Option[FinancialDataHIP]] =
    EitherT {
      val dateFrom: LocalDate = LocalDate.parse(appConfig.hipDateFrom.format(DateTimeFormatter.ISO_LOCAL_DATE))
      val dateTo: LocalDate   = LocalDate.now()
      val batchSize           = 999
      val dateRanges          = calculateDateRanges(dateFrom, dateTo, batchSize)
      println(s"date ranges--> $dateRanges")
      val results             = Future
        .sequence {
          dateRanges.map { case (batchStart, batchEnd) =>
            val formattedDateFrom = batchStart.toString
            val formattedDateTo   = batchEnd.toString
            hipConnector
              .getFinancialDetails(eclReference, formattedDateFrom, formattedDateTo)
              .map { response =>
                println(s"Connector response for range $formattedDateFrom to $formattedDateTo: $response")
                Right(response)
              }
              .recover {
                case UpstreamErrorResponse(_, NOT_FOUND, _, _)                  =>
                  Right(FinancialDataHIP(None, None))
                case UpstreamErrorResponse(message, UNPROCESSABLE_ENTITY, _, _) =>
                  val statCode = parseJsonCode(message)
                  if (statCode.contains("018")) {
                    logger.info(
                      s"Received HTTP 422 with code '018' for ECL-HIP reference: ${eclReference.value}. Returning No Data."
                    )
                    Right(FinancialDataHIP(None, None))

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
        }

      println(s"Results --> $results")

      val processedData: Future[Either[HipWrappedError, Option[FinancialDataHIP]]] = results
        .map { responses =>
          println(s"All responses: $responses")

          val failedResponses = responses.collect { case Left(error) => error }

          val successfulResponses = responses.collect { case Right(data) =>
            data
          }

          println(s"Failed responses: $failedResponses")
          println(s"Successful responses: $successfulResponses")

          if (failedResponses.nonEmpty) {
            println(s"failedResponses--> $failedResponses")
            println(s"failedResponses--> $failedResponses")
            Left(failedResponses.head)
          } else {
            val combinedData = combineFinancialData(successfulResponses)
            println(s"Combined FinancialDataHIP: $combinedData")
            println(s"Combined FinancialDataHIP JSON: ${Json.toJson(combinedData)}")
            Right(Some(combinedData))
          }
        }
        .map {
          case Right(Some(combinedData)) =>
            val filteredResult = filterOutUnknownDocumentTypes(combinedData)
            println(s"Filtered result: $filteredResult")
            println(Json.toJson(filteredResult))
            Right(Some(filteredResult))

          case other => other
        }
        .recover {
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

      processedData

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

  def calculateDateRanges(startDate: LocalDate, endDate: LocalDate, batchSize: Int): Seq[(LocalDate, LocalDate)] = {
    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt + 1
    val ranges      = (0 to daysBetween by batchSize).map { offset =>
      val batchStart = startDate.plusDays(offset)
      val batchEnd   = batchStart.plusDays(batchSize - 1).min(endDate)
      (batchStart, batchEnd)
    }
    ranges
  }

  def combineFinancialData(dataList: Seq[FinancialDataHIP]): FinancialDataHIP = {
    println(s"")
    val combinedDocumentDetails = dataList.flatMap(_.documentDetails).flatten
    val combineTotalisation     = dataList.lastOption.flatMap(_.totalisation)
    println(s"Combined document details: $combinedDocumentDetails")
    println(s"Combined totalisation: $combineTotalisation")
    FinancialDataHIP(
      totalisation = combineTotalisation,
      documentDetails = if (combinedDocumentDetails.nonEmpty) Some(combinedDocumentDetails) else None
    )
  }

}
