package uk.gov.hmrc.economiccrimelevyaccount.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyaccount.models.integrationframework.{FinancialDataErrorResponse, FinancialDataResponse}


trait IntegrationFrameworkStubs { self: WireMockStubs =>

  def stubFinancialData(financialDataResponse: FinancialDataResponse): StubMapping =
    stub(
      get(urlEqualTo(s"/enterprise/02.00.00/financial-data/zecl/$testEclRegistrationReference/ECL")),
      aResponse()
        .withStatus(OK)
        .withBody(Json.toJson(financialDataResponse).toString())
    )

  def stubFinancialDataNotFound(financialDataErrorResponse: FinancialDataErrorResponse): StubMapping =
    stub(
      get(urlEqualTo(s"/enterprise/02.00.00/financial-data/zecl/$testEclRegistrationReference/ECL")),
      aResponse()
        .withStatus(OK)
        .withBody(
          s"""
             |{
             |    "failures": [
             |        {
             |            "code": "NO_DATA_FOUND",
             |            "reason": "The remote endpoint has indicated that no data can be found."
             |        }
             |    ]
             |}
     """.stripMargin)
    )
}
