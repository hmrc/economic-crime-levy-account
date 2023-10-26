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

package uk.gov.hmrc.economiccrimelevyaccount.services

import cats.data.EitherT
import uk.gov.hmrc.economiccrimelevyaccount.connectors.DesConnector
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{Obligation, ObligationData}
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.DesSubmissionError
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class DesService @Inject() (
  desConnector: DesConnector,
  clock: Clock
)(implicit ec: ExecutionContext) {

  def getObligationData(
    eclRegistrationReference: String
  )(implicit hc: HeaderCarrier): EitherT[Future, DesSubmissionError, Option[ObligationData]] =
    EitherT {
      desConnector
        .getObligationData(eclRegistrationReference)
        .map { obligationDataOption =>
          Right(obligationDataOption.map(getObligationDataDueLessThanYearFromNow(_)))
        }
        .recover {
          case error @ UpstreamErrorResponse(message, code, _, _)
              if UpstreamErrorResponse.Upstream5xxResponse
                .unapply(error)
                .isDefined || UpstreamErrorResponse.Upstream4xxResponse.unapply(error).isDefined =>
            Left(DesSubmissionError.BadGateway(reason = message, code = code))
          case NonFatal(thr) => Left(DesSubmissionError.InternalUnexpectedError(thr.getMessage, Some(thr)))
        }
    }

  private def getObligationDataDueLessThanYearFromNow: ObligationData => ObligationData =
    obligationData => {
      val obligationDetails = obligationData.obligations
        .flatMap(_.obligationDetails)
        .filterNot(
          _.inboundCorrespondenceDueDate
            .isAfter(LocalDate.ofInstant(Instant.now(clock), ZoneOffset.UTC).plusYears(1))
        )
      val identification    = obligationData.obligations.headOption.flatMap(_.identification)

      ObligationData(Seq(Obligation(identification, obligationDetails)))
    }

}