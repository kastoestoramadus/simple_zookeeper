name := "simple_zookeeper"

version := "1.0"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
   "org.apache.curator" % "curator-framework" % "2.7.0",
   "org.apache.curator" % "curator-test" % "2.7.0",
   "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
   "org.testng" % "testng" % "6.8.8",
   "org.apache.zookeeper" % "zookeeper" % "3.4.5"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri"),
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

Revolver.settings

scalaVersion  := "2.11.2"

EclipseKeys.withSource := true

EclipseKeys.eclipseOutput := Some(".target")

EclipseKeys.useProjectId := true
