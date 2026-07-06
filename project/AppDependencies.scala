import sbt.*

object AppDependencies {

  private val hmrcBootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"  %% "bootstrap-backend-play-30" % hmrcBootstrapVersion exclude("org.apache.commons", "commons-lang3"),
    "io.lemonlabs" %% "scala-uri"                 % "4.0.3",
    "org.apache.commons" % "commons-lang3"    % "3.18.0",
    "ch.qos.logback"     % "logback-core"     % "1.5.27",
    "ch.qos.logback"     % "logback-classic"  % "1.5.27",
    "at.yawk.lz4"        % "lz4-java"         % "1.10.3"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % hmrcBootstrapVersion,
    "org.mockito"          %% "mockito-scala"          % "2.2.1",
    "org.scalatestplus"    %% "scalacheck-1-17"        % "3.2.18.0",
    "org.scalacheck"       %% "scalacheck"             % "1.17.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"  % "1.1.0",
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2"
  ).map(_ % "test")

  val itDependencies: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-17"      % "3.2.18.0"  % Test,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % hmrcBootstrapVersion % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test

}