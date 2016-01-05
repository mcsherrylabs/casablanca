
name := "demo-ui"

version := "1.0"

scalaVersion := "2.10.5"

scalacOptions ++= Seq("-deprecation", "-feature")


libraryDependencies +=  "com.twitter" %% "finatra" % "1.6.0"

//libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "com.stackmob" %% "newman" % "1.3.5"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "stepsoft" at "http://nexus.mcsherrylabs.com/nexus/content/groups/public"

