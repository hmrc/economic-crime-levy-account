package uk.gov.hmrc.economiccrimelevyaccount

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError

class FinancialDetailsISpec extends ISpecBase {

  s"GET ${routes.FinancialDataController.getFinancialData.url}" should {
    "return 200 OK with the financial data JSON when financial is returned" in {
      stubAuthorised()

      stubGetFinancialDetails()

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe OK
    }

    "return 401 UNAUTHORIZED when auth calls responds with unauthorized" in {
      stubUnauthorised()

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe UNAUTHORIZED
    }

    "return 502 BAD_GATEWAY when DES does not find obligations under the given ecl reference" in {
      stubAuthorised()

      val statusCode   = BAD_REQUEST
      val errorMessage = "bad request"
      stubGetFinancialDetailsUpstreamError(statusCode, errorMessage)

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result)        shouldBe BAD_GATEWAY
      contentAsJson(result) shouldBe Json.toJson(
        ResponseError.badGateway(errorMessage, statusCode)
      )
    }
  }
}
