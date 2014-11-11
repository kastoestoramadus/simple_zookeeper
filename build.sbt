name := "simple_zookeeper"

version := "1.0"

libraryDependencies ++= Seq(
   "org.apache.zookeeper" % "zookeeper" % "3.4.5"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
)

scalaVersion := "2.10.4"

EclipseKeys.withSource := true

EclipseKeys.eclipseOutput := Some(".target")

EclipseKeys.useProjectId := true
