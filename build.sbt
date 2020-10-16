name := "sbt-hyperlink"
organization := "de.cbley"

licenses := Seq("MIT" -> url("https://github.com/avdv/sbt-hyperlink/blob/master/LICENSE"))

enablePlugins(SbtPlugin)

scalaVersion in ThisBuild := "2.12.8"

// utest
libraryDependencies += "com.lihaoyi" %% "utest" % "0.7.5" % Test
testFrameworks += new TestFramework("utest.runner.Framework")

bintrayPackageLabels := Seq("sbt", "plugin")
bintrayVcsUrl := Some("""git@github.com:avdv/sbt-hyperlink.git""")

initialCommands in console := """import sbthyperlink._"""

// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
