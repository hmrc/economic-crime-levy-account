package uk.gov.hmrc.economiccrimelevyaccount

import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.economiccrimelevyaccount.base.ISpecBase

class HealthEndpointISpec
  extends ISpecBase {

  "GET /ping/ping" should {
    "respond with 200 status" in {
      val result = callRoute(FakeRequest(Call("GET", "/ping/ping")))

      status(result) shouldBe OK
    }
  }
}
