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
    "org.webjars.npm"    % "angular2"             % "2.0.0-beta.14"
  )
}

mainClass in Compile := Some("xyz.wiedenhoeft.azas.controllers.Boot")

resolveFromWebjarsNodeModulesDir := true

val copyModules = taskKey[Seq[File]]("Module files for runtime")

copyModules := {
  streams.value.log.info("Regenerating modules in managed resources")
  val targetDirs = (managedResourceDirectories in Compile).value
  val copy = {
    val base = (webJarsNodeModulesDirectory in Assets).value
    val files = (nodeModules in Assets).value
    files pair Path.rebase(base, "modules")
  } ++ {
    val base = target.value / "web" / "typescript" / "main" / "src" / "main" / "assets" / "app"
    val files = typescript.value
    files pair Path.rebase(base, (file("modules") / "azas").getPath)
  } flatMap {
    case (source, relativeDest) ⇒
      targetDirs map { targetDir ⇒
        (source, targetDir / relativeDest)
      }
  }
  IO.copy(copy, overwrite = true)
  copy map { case (_, dest) ⇒ dest }
}

(resourceGenerators in Compile) <+= copyModules

assemblyMergeStrategy in assembly := { path ⇒
  if (path.startsWith("modules")) {
    MergeStrategy.first
  } else if (path.startsWith("META-INF/resources/webjars")) {
    MergeStrategy.discard
  } else {
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(path)
  }
}
