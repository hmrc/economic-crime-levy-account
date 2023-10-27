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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.bta.{BtaTileData, DueReturn}
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{ObligationData, Open}
import uk.gov.hmrc.economiccrimelevyaccount.services.DesService
import uk.gov.hmrc.economiccrimelevyaccount.utils.CorrelationIdHelper
import uk.gov.hmrc.economiccrimelevyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class BtaTileDataController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  obligationDataService: DesService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with BaseController
    with ErrorHandler
    with CorrelationIdHelper {

  def getBtaTileData: Action[AnyContent] = authorise.async { implicit request =>
    implicit val hc: HeaderCarrier = getOrCreateCorrelationID(request)
    (for {
      obligationData <- obligationDataService.getObligationData(request.eclReference).asResponseError
      btaTilaData     = constructBtaTileData(request.eclReference, obligationData)
    } yield btaTilaData).convertToResultWithJsonBody(OK)
  }

  private def constructBtaTileData(
    eclReference: EclReference,
    obligationData: ObligationData
  ): BtaTileData = {
    val highestPriorityDueReturn = obligationData.obligations
      .flatMap(
        _.obligationDetails
          .filter(_.status == Open)
          .sortBy(_.inboundCorrespondenceDueDate)
      )
      .headOption

    highestPriorityDueReturn match {
      case Some(obligationDetails) =>
        BtaTileData(
          eclReference,
          Some(
            DueReturn(
              isOverdue = obligationDetails.isOverdue,
              dueDate = obligationDetails.inboundCorrespondenceDueDate,
              periodStartDate = obligationDetails.inboundCorrespondenceFromDate,
              periodEndDate = obligationDetails.inboundCorrespondenceToDate,
              fyStartYear = obligationDetails.inboundCorrespondenceFromDate.getYear.toString,
              fyEndYear = obligationDetails.inboundCorrespondenceToDate.getYear.toString
            )
          )
        )
      case None                    => BtaTileData(eclReference, None)
    }
  }

}
