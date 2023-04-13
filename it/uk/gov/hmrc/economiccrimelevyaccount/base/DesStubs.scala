package uk.gov.hmrc.economiccrimelevyaccount.base

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.economiccrimelevyaccount.base.WireMockHelper.stub
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData

trait DesStubs { self: WireMockStubs =>

  def stubGetObligations(obligationData: ObligationData): StubMapping =
    stub(
      get(urlEqualTo(s"/enterprise/obligation-data/zecl/$testEclRegistrationReference/ECL")),
      aResponse()
        .withStatus(OK)
        .withBody(Json.toJson(obligationData).toString())
    )

  def stubObligationsNotFound(): StubMapping =
    stub(
      get(urlEqualTo(s"/enterprise/obligation-data/zecl/$testEclRegistrationReference/ECL")),
      aResponse()
        .withStatus(NOT_FOUND)
        .withBody("No obligation data found")
    )

}
