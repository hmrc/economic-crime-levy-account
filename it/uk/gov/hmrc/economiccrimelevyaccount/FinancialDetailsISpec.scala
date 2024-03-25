package uk.gov.hmrc.economiccrimelevyaccount

import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.ResponseError
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.Eventually.eventually
import play.api.http.Status.{CONFLICT, UNPROCESSABLE_ENTITY}
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase
import uk.gov.hmrc.economiccrimelevyaccount.controllers.routes
import uk.gov.hmrc.economiccrimelevyaccount.models.CustomHeaderNames
import uk.gov.hmrc.http.HeaderNames

class FinancialDetailsISpec extends ISpecBase {

  private val getFinancialDetailsRegex = "/penalty/financial-data/ZECL/.*"

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

    "return 502 BAD_GATEWAY with the financial data JSON when 409 CONFLICT is returned from financial details" in {
      resetAllRequests()
      stubAuthorised()

      stubGetFinancialDetailsUpstreamError(
        CONFLICT,
        "{\"failures\":[{\"code\":\"DUPLICATE_SUBMISSION\",\"reason\":\"The remote endpoint has indicated duplicate submission.\"}]}"
      )

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe BAD_GATEWAY

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

    "return 502 BAD_GATEWAY with the financial data JSON when 422 UNPROCESSABLE_ENTITY is returned from financial details" in {
      resetAllRequests()
      stubAuthorised()

      stubGetFinancialDetailsUpstreamError(
        UNPROCESSABLE_ENTITY,
        "{\"failures\":[{\"code\":\"INVALID_ID\",\"reason\":\"The remote endpoint has indicated that reference id is invalid.\"}]}"
      )

      val result = callRoute(
        FakeRequest(routes.FinancialDataController.getFinancialData)
      )

      status(result) shouldBe BAD_GATEWAY

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
