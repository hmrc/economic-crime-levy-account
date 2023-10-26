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

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.economiccrimelevyaccount.services.DesService
import uk.gov.hmrc.economiccrimelevyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class ObligationDataController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  obligationDataService: DesService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private val loggerContext                 = "ObligationDataController"
  def getObligationData: Action[AnyContent] = authorise.async { implicit request =>
    obligationDataService
      .getObligationData(request.eclRegistrationReference)
      .map {
        case Some(obligationData) =>
          logger.info(
            s"$loggerContext - Successful call to DES for obligation data for eclReference: ${request.eclRegistrationReference}"
          )
          Ok(Json.toJson(obligationData))
        case None                 =>
          logger.error(
            s"$loggerContext - No obligation data found for eclReference ${request.eclRegistrationReference}"
          )
          NotFound("No obligation data found")
      }
  }

}
