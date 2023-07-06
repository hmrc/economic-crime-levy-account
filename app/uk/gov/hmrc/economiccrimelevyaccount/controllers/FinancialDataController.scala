package uk.gov.hmrc.economiccrimelevyaccount.controllers

import play.api.mvc._
import uk.gov.hmrc.economiccrimelevyaccount.connectors.IntegrationFrameworkConnector
import uk.gov.hmrc.economiccrimelevyreturns.controllers.actions.AuthorisedAction
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FinancialDataController @Inject() (
  cc: ControllerComponents,
  authorise: AuthorisedAction,
  integrationFramework: IntegrationFrameworkConnector
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def getFinancialData: Action[AnyContent] = authorise.async { implicit request =>
    integrationFramework.getFinancialDetails(request.eclRegistrationReference)
      .map{
        case Left(e) => InternalServerError(e)
        case Right(value) => Ok(value)
      }
  }
}
