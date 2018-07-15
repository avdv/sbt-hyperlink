name := "sbt-hyperlink"
organization := "de.cbley"

licenses := Seq("MIT" -> url("https://github.com/avdv/sbt-hyperlink/blob/master/LICENSE"))

sbtPlugin := true

// utest
libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.4" % Test
testFrameworks += new TestFramework("utest.runner.Framework")

bintrayPackageLabels := Seq("sbt","plugin")
bintrayVcsUrl := Some("""git@github.com:avdv/sbt-hyperlink.git""")

initialCommands in console := """import sbthyperlink._"""

// set up 'scripted; sbt plugin for testing sbt plugins
scriptedLaunchOpts ++=
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
