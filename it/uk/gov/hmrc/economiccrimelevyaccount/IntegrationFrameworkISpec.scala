package uk.gov.hmrc.economiccrimelevyaccount

import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{FinancialDataErrorResponse, FinancialDataResponse}
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._

class IntegrationFrameworkISpec extends ISpecBase{

  s"GET ${routes.FinancialDataController.getFinancialData.url}" should {
    "return 200 OK with the financial data JSON when financial data is returned" in {
      stubAuthorised()

      val financialData = random[FinancialDataResponse]

      stubFinancialData(financialData)

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe OK

      contentAsJson(result) shouldBe Json.toJson(financialData)
    }

    "return 200 OK with financial data error response that includes code and reason for error" in {
      stubAuthorised()

      val financialErrorData = random[FinancialDataErrorResponse]

      stubFinancialDataNotFound(financialErrorData)

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe OK

      contentAsJson(result) shouldBe Json.toJson(financialErrorData)
    }
  }
}
