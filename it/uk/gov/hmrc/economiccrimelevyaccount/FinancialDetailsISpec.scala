package uk.gov.hmrc.economiccrimelevyaccount

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.FinancialData

class FinancialDetailsISpec extends ISpecBase {

  s"GET ${routes.FinancialDataController.getFinancialData.url}" should {
    "return 200 OK with the financial data JSON when financial is returned" in {
      stubAuthorised()

      val financialDetails = random[FinancialData]

      stubGetFinancialDetails(financialDetails)

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe OK
    }
  }
}
