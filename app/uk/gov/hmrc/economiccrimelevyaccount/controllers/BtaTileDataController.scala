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
import uk.gov.hmrc.economiccrimelevyaccount.models.bta.{BtaTileData, DueReturn}
import uk.gov.hmrc.economiccrimelevyaccount.models.des.{ObligationData, Open}
import uk.gov.hmrc.economiccrimelevyaccount.services.ObligationDataService
import uk.gov.hmrc.economiccrimelevyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class BtaTileDataController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  obligationDataService: ObligationDataService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def getBtaTileData: Action[AnyContent] = authorise.async { implicit request =>
    obligationDataService
      .getObligationData(request.eclRegistrationReference)
      .map { obligationData =>
        val btaTileData: BtaTileData = constructBtaTileData(request.eclRegistrationReference, obligationData)
        Ok(Json.toJson(btaTileData))
      }
  }

  private def constructBtaTileData(
    eclRegistrationReference: String,
    obligationData: Option[ObligationData]
  ): BtaTileData =
    obligationData match {
      case None    => BtaTileData(eclRegistrationReference, None)
      case Some(o) =>
        val highestPriorityDueReturn = o.obligations
          .flatMap(
            _.obligationDetails
              .filter(_.status == Open)
              .sortBy(_.inboundCorrespondenceDueDate)
          )
          .headOption

        highestPriorityDueReturn match {
          case Some(obligationDetails) =>
            BtaTileData(
              eclRegistrationReference,
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
          case None                    => BtaTileData(eclRegistrationReference, None)
        }
    }

}
