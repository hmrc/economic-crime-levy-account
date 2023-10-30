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

package uk.gov.hmrc.economiccrimelevyaccount.controllers

import cats.data.EitherT
import play.api.Logging
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.{BadGateway, DesSubmissionError, IntegrationFrameworkError, InternalServiceError, ResponseError}

import scala.concurrent.{ExecutionContext, Future}

trait ErrorHandler extends Logging {

  implicit class ErrorConvertor[E, R](value: EitherT[Future, E, R]) {

    def asResponseError(implicit c: Converter[E], ec: ExecutionContext): EitherT[Future, ResponseError, R] =
      value.leftMap(c.convert).leftSemiflatTap {
        case InternalServiceError(message, _, cause) =>
          val causeText = cause
            .map { ex =>
              s"""
                   |Message: ${ex.getMessage}
                   |Trace: ${ex.getStackTrace.mkString(System.lineSeparator())}
                   |""".stripMargin
            }
            .getOrElse("No exception is available")
          logger.error(s"""Internal Server Error: $message
               |
               |$causeText""".stripMargin)
          Future.successful(())
        case BadGateway(message, _, responseCode)    =>
          val causeText = s"""
                 |Message: $message
                 |Upstream status code: $responseCode
                 |""".stripMargin

          logger.error(s"""Bad gateway: $message
               |
               |$causeText""".stripMargin)
          Future.successful(())
        case _                                       => Future.successful(())
      }
  }

  trait Converter[E] {
    def convert(error: E): ResponseError
  }

  implicit val desSubmissionErrorConverter: Converter[DesSubmissionError] =
    new Converter[DesSubmissionError] {
      override def convert(error: DesSubmissionError): ResponseError = error match {
        case DesSubmissionError.NotFound(eclReference)                  =>
          ResponseError.notFoundError(s"Unable to find record with id: ${eclReference.value}")
        case DesSubmissionError.BadGateway(message, code)               =>
          ResponseError.badGateway(message = message, code = code)
        case DesSubmissionError.InternalUnexpectedError(message, cause) =>
          ResponseError.internalServiceError(message = message, cause = cause)
      }
    }

  implicit val Converter: Converter[IntegrationFrameworkError] =
    new Converter[IntegrationFrameworkError] {
      override def convert(error: IntegrationFrameworkError): ResponseError = error match {
        case IntegrationFrameworkError.NotFound(eclReference)                  =>
          ResponseError.notFoundError(s"Unable to find record with id: ${eclReference.value}")
        case IntegrationFrameworkError.BadGateway(message, code)               =>
          ResponseError.badGateway(message = message, code = code)
        case IntegrationFrameworkError.InternalUnexpectedError(message, cause) =>
          ResponseError.internalServiceError(message = message, cause = cause)
      }
    }
}
