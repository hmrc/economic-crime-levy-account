package uk.gov.hmrc.economiccrimelevyaccount

import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes

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
  }
}
