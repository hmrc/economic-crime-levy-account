import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys

val appName = "economic-crime-levy-account"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings): _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(majorVersion := 0)
  .settings(inThisBuild(buildSettings))
  .settings(scoverageSettings: _*)
  .settings(scalaCompilerOptions: _*)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(
    scalaVersion := "2.13.16",
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    PlayKeys.playDefaultPort := 14009,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    (update / evictionWarningOptions).withRank(KeyRanks.Invisible) :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )

lazy val buildSettings = Def.settings(
  scalafmtOnCompile := true,
  useSuperShell := false
)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "test",
    baseDirectory.value / "test-common"
  ),
  fork := true
)

lazy val itSettings: Seq[Def.Setting[_]] = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-common"
  ),
  parallelExecution := false,
  fork := true
)

val excludedScoveragePackages: Seq[String] = Seq(
  "<empty>",
  "Reverse.*",
  ".*handlers.*",
  "uk.gov.hmrc.BuildInfo",
  "app.*",
  "prod.*",
  ".*Routes.*",
  ".*testonly.*",
  "testOnlyDoNotUseInAppConf.*"
)

val scoverageSettings: Seq[Setting[_]] = Seq(
  ScoverageKeys.coverageExcludedFiles := excludedScoveragePackages.mkString(";"),
  ScoverageKeys.coverageMinimumStmtTotal := 90,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true
)

val scalaCompilerOptions: Def.Setting[Task[Seq[String]]] = scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Wconf:cat=feature:ws,cat=optimizer:ws,src=target/.*:s",
  "-Xlint:-byname-implicit"
)

addCommandAlias("runAllChecks", ";clean;compile;scalafmtCheckAll;coverage;test;it:test;scalastyle;coverageReport")
