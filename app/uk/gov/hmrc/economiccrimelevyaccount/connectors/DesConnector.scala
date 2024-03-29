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

package uk.gov.hmrc.economiccrimelevyaccount.connectors

import com.typesafe.config.Config
import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import uk.gov.hmrc.economiccrimelevyaccount.config.AppConfig
import uk.gov.hmrc.economiccrimelevyaccount.models.{CustomHeaderNames, EclReference}
import uk.gov.hmrc.economiccrimelevyaccount.models.des.ObligationData
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, Retries, StringContextOps}

import java.time.{LocalDate, ZoneOffset}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2,
  override val configuration: Config,
  override val actorSystem: ActorSystem
)(implicit ec: ExecutionContext)
    extends BaseConnector
    with Retries
    with Logging {

  private def desUrl(eclRegistrationReference: String) =
    s"${appConfig.desUrl}/enterprise/obligation-data/zecl/$eclRegistrationReference/ECL?from=2022-04-01&to=${LocalDate.now(ZoneOffset.UTC).toString}"

  def getObligationData(
    eclRegistrationReference: EclReference
  )(implicit hc: HeaderCarrier): Future[ObligationData] = {
    val correlationId = hc.headers(scala.Seq(CustomHeaderNames.xCorrelationId)) match {
      case Seq((_, id)) =>
        id
      case _            =>
        UUID.randomUUID().toString
    }

    retryFor[ObligationData]("DES - obligation data")(retryCondition) {
      httpClient
        .get(url"${desUrl(eclRegistrationReference.value)}")
        .setHeader((HeaderNames.authorisation, s"Bearer ${appConfig.desBearerToken}"))
        .setHeader((CustomHeaderNames.environment, appConfig.desEnvironment))
        .setHeader((CustomHeaderNames.correlationId, correlationId))
        .executeAndDeserialise[ObligationData]
    }
  }
}
