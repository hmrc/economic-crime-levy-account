/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.economiccrimelevyaccount.base

import uk.gov.hmrc.economiccrimelevyaccount.EclTestData
import uk.gov.hmrc.economiccrimelevyaccount.generators.Generators

trait WireMockStubs extends EclTestData with Generators with AuthStubs with IntegrationFrameworkStubs
