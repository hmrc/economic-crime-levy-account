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

package uk.gov.hmrc.economiccrimelevyaccount.config

import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  implicit val dateLoader: ConfigLoader[LocalDate] = ConfigLoader(_.getString).map(LocalDate.parse(_))

  val appName: String = config.get[String]("appName")

  val desBearerToken: String = config.get[String]("microservice.services.des.bearerToken")

  val desEnvironment: String = config.get[String]("microservice.services.des.environment")

  val desUrl: String = servicesConfig.baseUrl("des")

  val integrationFrameworkUrl: String = servicesConfig.baseUrl("integration-framework")

  val integrationFrameworkBearerToken: String =
    config.get[String]("microservice.services.integration-framework.bearerToken")

  val integrationFrameworkEnvironment: String =
    config.get[String]("microservice.services.integration-framework.environment")

  val integrationFrameworkDateFrom: LocalDate =
    config.get[LocalDate]("microservice.services.integration-framework.dateFrom")
}
