import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import WebKeys._

lazy val azas = (project in file("."))
  .enablePlugins(SbtWeb)

name := "azas"

organization := "xyz.wiedenhoeft"

version := "1.2-SNAPSHOT"

scalaVersion := "2.11.8"

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(PreserveSpaceBeforeArguments, true)

libraryDependencies ++= {
  val akkaVersion = "2.4.3"
  val sprayVersion = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "io.spray"          %% "spray-can"            % sprayVersion,
    "io.spray"          %% "spray-routing"        % sprayVersion,
    "io.spray"          %% "spray-http"           % sprayVersion,
    "io.spray"          %% "spray-httpx"          % sprayVersion,
    "io.spray"          %% "spray-json"           % "1.3.2",
    "mysql"              % "mysql-connector-java" % "5.1.38",
    "com.h2database"     % "h2"                   % "1.4.191",
    "com.typesafe"       % "config"               % "1.3.0",
    "org.scalatest"     %% "scalatest"            % "2.2.6"       % "test",
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion   % "test",
    "io.spray"          %% "spray-testkit"        % sprayVersion  % "test",

    "org.webjars.npm"    % "systemjs"             % "0.19.26",
    "org.webjars.npm"    % "rxjs"                 % "5.0.0-beta.5",
    "org.webjars.npm"    % "angular2"             % "2.0.0-beta.14",
    "org.webjars.npm"    % "reflect-metadata"     % "0.1.3",
    "org.webjars.npm"    % "es6-shim"             % "0.35.0"
  )
}

mainClass in Compile := Some("xyz.wiedenhoeft.azas.controllers.Boot")

resolveFromWebjarsNodeModulesDir := true

unmanagedResourceDirectories in Compile += (nodeModuleDirectory in Assets).value
