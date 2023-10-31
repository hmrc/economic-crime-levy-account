/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.economiccrimelevyaccount.controllers

import cats.data.EitherT
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.economiccrimelevyaccount.base.SpecBase
import uk.gov.hmrc.economiccrimelevyaccount.generators.CachedArbitraries._
import uk.gov.hmrc.economiccrimelevyaccount.models.EclReference
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData
import uk.gov.hmrc.economiccrimelevyaccount.models.errors.DesError
import uk.gov.hmrc.economiccrimelevyaccount.services.DesService

import scala.concurrent.Future

class ObligationDataControllerSpec extends SpecBase {

  val mockObligationDataService: DesService = mock[DesService]

  val controller = new ObligationDataController(
    cc,
    fakeAuthorisedAction,
    mockObligationDataService
  )

  "getObligationData" should {
    "return 200 OK with the obligation data JSON when obligation data is returned by the service" in forAll {
      obligationData: ObligationData =>
        when(mockObligationDataService.getObligationData(any[String].asInstanceOf[EclReference])(any()))
          .thenReturn(EitherT.rightT[Future, DesError](obligationData))

        val result: Future[Result] =
          controller.getObligationData()(fakeRequest)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(obligationData)
    }

    "return 404 NOT_FOUND when obligation data is not returned by the service" in forAll { eclReference: EclReference =>
      when(mockObligationDataService.getObligationData(any[String].asInstanceOf[EclReference])(any()))
        .thenReturn(EitherT.leftT[Future, ObligationData](DesError.NotFound(eclReference)))

      val result: Future[Result] =
        controller.getObligationData()(fakeRequest)

      status(result) shouldBe NOT_FOUND
    }
  }

}
