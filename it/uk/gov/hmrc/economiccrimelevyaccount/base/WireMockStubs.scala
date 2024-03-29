/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.economiccrimelevyaccount.base

import uk.gov.hmrc.economiccrimelevyaccount.EclTestData

trait WireMockStubs extends EclTestData with AuthStubs with DesStubs with IntegrationFrameworkStubs
