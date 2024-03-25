package uk.gov.hmrc.economiccrimelevyaccount

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.prop.Tables._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.models.CustomHeaderNames
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError
import uk.gov.hmrc.http.HeaderNames

class FinancialDetailsISpec extends ISpecBase {

  s"GET ${routes.FinancialDataController.getFinancialData.url}" should {
    "return 200 OK with the financial data JSON when financial is returned" in {
      resetAllRequests()
      stubAuthorised()

      stubGetFinancialDetailsSuccess()

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe OK

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getFinancialDetailsRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
      }
    }

    "return 502 BAD_GATEWAY when IF response with an upstream error" in forAll(
      Table(
        "statusCode",
        BAD_REQUEST,
        CONFLICT,
        INTERNAL_SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        UNPROCESSABLE_ENTITY
      )
    ) { (statusCode: Int) =>
      resetAllRequests()
      stubAuthorised()

      val errorMessage = "Bad Request"
      stubGetFinancialDetailsUpstreamError(statusCode, errorMessage)

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result)        shouldBe BAD_GATEWAY
      contentAsJson(result) shouldBe Json.toJson(
        ResponseError.badGateway(s"Get Financial Data Failed - $errorMessage", statusCode)
      )

      eventually {
        verify(
          1,
          getRequestedFor(urlMatching(getFinancialDetailsRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
      }
    }

    "return 401 UNAUTHORIZED when auth calls responds with unauthorized" in {
      resetAllRequests()
      stubUnauthorised()

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe UNAUTHORIZED

      eventually {
        verify(
          0,
          getRequestedFor(urlMatching(getFinancialDetailsRegex))
            .withHeader(HeaderNames.authorisation, equalTo(s"Bearer ${appConfig.integrationFrameworkBearerToken}"))
            .withHeader(CustomHeaderNames.environment, equalTo(appConfig.integrationFrameworkEnvironment))
            .withHeader(CustomHeaderNames.correlationId, matching(uuidRegex))
            .withHeader(CustomHeaderNames.xCorrelationId, matching(uuidRegex))
        )
      }
    }
  }
}
