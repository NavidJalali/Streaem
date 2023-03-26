ThisBuild / scalaVersion     := "3.2.2"
ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "io.navidjalali"
ThisBuild / organizationName := "navidjalali"
ThisBuild / versionScheme    := Some("early-semver")

val Versions = new {
  val zio         = "2.0.10"
  val tapir       = "1.2.10"
  val sttp        = "3.8.13"
  val apiSpec     = "0.3.2"
  val blazeServer = "0.23.14"
  val zioJson     = "0.5.0"
  val catsInterop = "23.0.0.2"
  val zioLogging  = "2.1.11"
  val sl4j2       = "2.0.5"
}

lazy val root = (project in file("."))
  .settings(
    name := "streaem",
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio"                     % Versions.zio,
      "dev.zio"                       %% "zio-streams"             % Versions.zio,
      "dev.zio"                       %% "zio-json"                % Versions.zioJson,
      "dev.zio"                       %% "zio-interop-cats"        % Versions.catsInterop,
      "dev.zio"                       %% "zio-logging"             % Versions.zioLogging,
      "dev.zio"                       %% "zio-logging-slf4j2"      % Versions.zioLogging,
      "org.slf4j"                      % "slf4j-simple"            % Versions.sl4j2,
      "com.softwaremill.sttp.client3" %% "core"                    % Versions.sttp,
      "com.softwaremill.sttp.client3" %% "zio"                     % Versions.sttp,
      "com.softwaremill.sttp.client3" %% "zio-json"                % Versions.sttp,
      "com.softwaremill.sttp.tapir"   %% "tapir-core"              % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-zio"               % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server-zio" % Versions.tapir,
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"      % Versions.tapir,
      "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"      % Versions.apiSpec,
      "org.http4s"                    %% "http4s-blaze-server"     % Versions.blazeServer,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"          % Versions.tapir,
      "dev.zio"                       %% "zio-test"                % Versions.zio % Test,
      "dev.zio"                       %% "zio-test-sbt"            % Versions.zio % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    assembly / assemblyJarName := "server.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs map {
          _.toLowerCase
        } match {
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.discard
        }
      case PathList("module-info.class") =>
        MergeStrategy.last
      case path if path.endsWith("/module-info.class") =>
        MergeStrategy.last
      case _ =>
        MergeStrategy.first
    },
    javaOptions ++= Seq(
      "-XX:+PrintGCDetails",
      "-XX:+UseG1GC",
      "-XX:+UseStringDeduplication",
      "--enable-preview"
    )
  )
