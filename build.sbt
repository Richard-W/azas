import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import WebKeys._
import LessKeys._
import com.typesafe.sbt.packager.linux.{LinuxFileMetaData, LinuxPackageMapping}

lazy val azas = (project in file("."))
  .enablePlugins(
    SbtWeb,
    LinuxPlugin,
    RpmPlugin,
    DebianPlugin
  )

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
version := "2.0-RC1"

scalaVersion := "2.11.8"

scalacOptions := Seq("-target:jvm-1.7")

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
  val akkaVersion = "2.4.8"
  val sprayVersion = "1.3.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "io.spray"          %% "spray-can"            % sprayVersion,
    "io.spray"          %% "spray-routing"        % sprayVersion,
    "io.spray"          %% "spray-http"           % sprayVersion,
    "io.spray"          %% "spray-httpx"          % sprayVersion,
    "io.spray"          %% "spray-json"           % "1.3.2",
    "mysql"              % "mysql-connector-java" % "6.0.3",
    "com.h2database"     % "h2"                   % "1.4.192",
    "com.typesafe"       % "config"               % "1.3.0",
    "org.scalatest"     %% "scalatest"            % "3.0.0"       % "test",
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion   % "test",
    "io.spray"          %% "spray-testkit"        % sprayVersion  % "test",

    "org.webjars.npm"    % "systemjs"             % "0.19.35",
    "org.webjars.npm"    % "rxjs"                 % "5.0.0-beta.5",
    "org.webjars.npm"    % "angular2"             % "2.0.0-beta.15",
    "org.webjars.npm"    % "es6-shim"             % "0.35.1"
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
  val copy = Seq(
    /* Map node modules from webjars */
    (
      (webJarsNodeModulesDirectory in Assets).value,
      (nodeModules in Assets).value,
      Some(file("modules"))
    ),
    /* Map javascripts generated by typescript */
    (
      target.value / "web" / "typescript" / "main" / "modules",
      typescript.value,
      Some(file("modules"))
    ),
    /* Map stylesheets generated by less */
    (
      target.value / "web" / "less" / "main",
      (less in Assets).value,
      None
    ),
    /* Map all files not processed */
    (
      baseDirectory.value / "src" / "main" / "assets",
      (baseDirectory.value / "src" / "main" / "assets" ** "*").get,
      None
    )
  ) flatMap {
    case (oldBase, files, Some(newBase)) ⇒
      files pair Path.rebase(oldBase, file("assets") / newBase.getPath)
    case (oldBase, files, None) ⇒
      files pair Path.rebase(oldBase, file("assets"))
  } flatMap {
    /* Make the relative paths absolute */
    case (source, relativeDest) ⇒
      targetDirs map { targetDir ⇒
        (source, targetDir / relativeDest.getPath)
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

/* Native packager settings */

linuxPackageMappings += LinuxPackageMapping(Seq(
  (assembly.value, "/usr/lib/azas.jar"),
  (baseDirectory.value / "dist" / "systemd-config", "/etc/sysconfig/azas"),
  (baseDirectory.value / "dist" / "systemd-service", "/usr/lib/systemd/system/azas.service")
), fileData = LinuxFileMetaData(permissions = "644", user = "root"))

linuxPackageMappings += LinuxPackageMapping(Seq(
  (baseDirectory.value / "example.conf", "/etc/azas.conf")
), fileData = LinuxFileMetaData(permissions = "640", user = "azas"))

linuxPackageMappings += LinuxPackageMapping(Seq(
  (baseDirectory.value / "dist" / "azas-launcher.sh", "/usr/bin/azas")
), fileData = LinuxFileMetaData(permissions = "755", user = "root"))

/* RPM specific settings */

rpmVendor := "generic"

version in Rpm := {
  if (version.value.endsWith("-SNAPSHOT")) version.value.split("\\-").head
  else version.value
}

rpmPre := Some("""
  |if [ $1 == 1 ]; then
  |  groupadd -g 126119 azas
  |  useradd -r -d /var/lib/azas -m -u 126119 -g 126119 azas
  |  chown -R azas:azas /var/lib/azas
  |  chmod -R 750 /var/lib/azas
  |fi
""".stripMargin)

rpmPost := Some("""
  |if [ $1 == 2 ]; then
  |  systemctl try-restart azas
  |fi
  |systemctl daemon-reload
""".stripMargin)

rpmPreun := Some("""
  |if [ $1 == 0 ]; then
  |  systemctl stop azas
  |fi
""".stripMargin)

rpmPostun := Some("""
  |if [ $1 == 0 ]; then
  |  userdel azas
  |  rm -rf /var/lib/azas
  |  systemctl daemon-reload
  |fi
""".stripMargin)

rpmLicense := Some("AGPLv3")

linuxPackageMappings in Rpm := linuxPackageMappings.value

/* Debian settings */

maintainer := "Richard Wiedenhöft <richard@wiedenhoeft.xyz>"

maintainerScripts in Debian := maintainerScriptsAppend((maintainerScripts in Debian).value)(
  "preinst" ->
    """
      |addGroup azas 126119
      |addUser azas 126119 azas "AZAS system user"
    """.stripMargin,
  "postinst" ->
    """
      |systemctl daemon-reload
      |systemctl try-restart azas
      |mkdir -p /var/lib/azas
      |chown -R azas:azas /var/lib/azas
      |chmod -R 750 /var/lib/azas
      |chown root:root /etc/azas.conf
      |chmod 600 /etc/azas.conf
    """.stripMargin,
  "prerm" ->
    """
      |systemctl stop azas
    """.stripMargin,
  "postrm" ->
    """
      |deleteUser azas
      |deleteGroup azas
    """.stripMargin
)
