import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import WebKeys._
import LessKeys._

lazy val azas = (project in file("."))
  .enablePlugins(SbtWeb)

name := "azas"

organization := "xyz.wiedenhoeft"

/* This version must follow the semantic versioning guidelines (http://semver.org).
 * Development versions are always suffixed with "-SNAPSHOT". All versions on the
 * master branch must have this suffix.
 *
 * If you want to release a new version create a git branch named "v<major>.<minor>" (e.g. "v1.1").
 * On this branch you should first specify a release candidate like 1.1-RC1 and tag it accordingly
 * using "git tag -a v1.1-RC1". Test this version extensively. When you are confident enough that
 * it is correct and bug-free you can set the version number to x.y.0 (e.g. "1.1.0") and tag it
 * accordingly.
 *
 * When you fix bugs against a release version you should fix them on master first and cherry-pick
 * the commits on the vx.y branch. Increment either the RC number or the 3rd version number when
 * you release again.
 *
 * Tags that are already pushed to upstream must never be changed. The code base for a specific
 * version is fixed and you can only fix bugs in minor releases by incrementing the patch version number
 * (z from x.y.z)
 */
version := "1.2-SNAPSHOT"

scalaVersion := "2.11.8"

/* Configure formatter plugin to
 * -> Align single line case statements
 * -> Preserve spaces before arguments for more beautiful scalatest statements
 */
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(PreserveSpaceBeforeArguments, true)

/* Specify the dependencies of this project. All coordinates here will
 * be retrieved either from the web or your local cache.
 */
libraryDependencies ++= {
  val akkaVersion = "2.4.4"
  val sprayVersion = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "io.spray"          %% "spray-can"            % sprayVersion,
    "io.spray"          %% "spray-routing"        % sprayVersion,
    "io.spray"          %% "spray-http"           % sprayVersion,
    "io.spray"          %% "spray-httpx"          % sprayVersion,
    "io.spray"          %% "spray-json"           % "1.3.2",
    "mysql"              % "mysql-connector-java" % "6.0.2",
    "com.h2database"     % "h2"                   % "1.4.191",
    "com.typesafe"       % "config"               % "1.3.0",
    "org.scalatest"     %% "scalatest"            % "2.2.6"       % "test",
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion   % "test",
    "io.spray"          %% "spray-testkit"        % sprayVersion  % "test",

    "org.webjars.npm"    % "systemjs"             % "0.19.26",
    "org.webjars.npm"    % "rxjs"                 % "5.0.0-beta.5",
    "org.webjars.npm"    % "angular2"             % "2.0.0-beta.15"
  )
}

/* Used by sbt-assembly to create a runnable jar */
mainClass in Compile := Some("xyz.wiedenhoeft.azas.controllers.Boot")

/* Resolve typescript imports against webjars */
resolveFromWebjarsNodeModulesDir := true

val copyModules = taskKey[Seq[File]]("Copy webjar contents and app to managed resources")

copyModules := {
  streams.value.log.info("Regenerating modules in managed resources")
  val targetDirs = (managedResourceDirectories in Compile).value
  val copy = {
    /* Map the node modules from webjars */
    val base = (webJarsNodeModulesDirectory in Assets).value
    val files = (nodeModules in Assets).value
    files pair Path.rebase(base, "modules")
  } ++ {
    /* Map the application that is built inside this file to the azas-module */
    val base = target.value / "web" / "typescript" / "main" / "src" / "main" / "assets" / "app"
    val files = typescript.value
    files pair Path.rebase(base, (file("modules") / "azas").getPath)
  } ++ {
    /* Map the stylesheets that are built using less */
    val base = target.value / "web" / "less" / "main" / "style"
    val files = (less in Assets).value
    files pair Path.rebase(base, file("css").getPath)
  } flatMap {
    /* Make the relative paths absolute */
    case (source, relativeDest) ⇒
      targetDirs map { targetDir ⇒
        (source, targetDir / relativeDest)
      }
  }
  /* Use the mappings to copy the files to their destination */
  IO.copy(copy, overwrite = true)
  /* Return the list of destination files */
  copy map { case (_, dest) ⇒ dest }
}

/* Register copyModules as a resource generator */
(resourceGenerators in Compile) <+= copyModules

/* Specify merge strategy for jar generation */
assemblyMergeStrategy in assembly := { path ⇒
  if (path.startsWith("modules")) {
    /* Webjars are constant for this project. We can just omit duplicates */
    MergeStrategy.first
  } else if (path.startsWith("META-INF/resources/webjars")) {
    /* By default webjar contents are copied to META-INF/resources/webjars.
     * As we are taking care of that ourselves we may just discard this dir
     * for a cleaner jar structure.
     */
    MergeStrategy.discard
  } else {
    /* Fall back to old strategy for paths not explicitly handled here */
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(path)
  }
}
