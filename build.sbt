import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

lazy val root = project.in(file(".")).
  aggregate(azasJS, azasJVM)
  .disablePlugins(AssemblyPlugin)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val azas = crossProject.in(file("."))
  .settings(
    name := "azas",
    organization := "xyz.wiedenhoeft",
    version := "1.2-SNAPSHOT",
    scalaVersion := "2.11.8"
  )
  .settings(scalariformSettings: _*)
  .settings(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
  )

lazy val azasJVM = azas.jvm
  .settings(
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
        "io.spray"          %% "spray-testkit"        % sprayVersion  % "test"
      )
    },
    mainClass in Compile := Some("xyz.wiedenhoeft.azas.controllers.Boot"),
    resources in Compile += (fullOptJS in azasJS in Compile).value.data,
    test in assembly := {}
  )

lazy val azasJS = azas.js
  .disablePlugins(AssemblyPlugin)
  .settings(
    publish := {},
    publishLocal := {}
  )

