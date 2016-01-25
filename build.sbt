name := "azas"

organization := "xyz.wiedenhoeft"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.1"
  val sprayVersion = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "io.spray"          %% "spray-can"      % sprayVersion,
    "io.spray"          %% "spray-routing"  % sprayVersion,
    "io.spray"          %% "spray-http"     % sprayVersion,
    "io.spray"          %% "spray-httpx"    % sprayVersion,
    "io.spray"          %% "spray-json"     % "1.3.2",
    "org.scalatest"     %% "scalatest"      % "2.2.6"       % "test",
    "com.typesafe.akka" %% "akka-testkit"   % akkaVersion   % "test",
    "io.spray"          %% "spray-testkit"  % sprayVersion  % "test"
  )
}

import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(PreserveSpaceBeforeArguments, true)
