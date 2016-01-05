import com.typesafe.sbt.packager.archetypes._


packagerSettings

JavaAppPackaging.settings

mappings in Universal += { file("bin/casablanca") -> "bin/casablanca" }

mappings in Universal += { file("bin/demo") -> "bin/demo" }

mappings in Universal += { file("conf/production-application.conf") -> "conf/production-application.conf" }

scalaVersion := "2.10.5"

version := "1.0"

scalacOptions ++= Seq("-deprecation", "-feature")

//libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies += "mcsherrylabs.com" %% "sss-db" % "0.9"

libraryDependencies += "mcsherrylabs.com" %% "sss-ancillary" % "0.9"

libraryDependencies += "mcsherrylabs.com" %% "sss-casablanca" % "0.7.0-SNAPSHOT"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.3.2"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

//libraryDependencies +=  "com.twitter" %% "finatra" % "1.6.0"

//libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

//libraryDependencies += "com.stackmob" %% "newman" % "1.3.5"

libraryDependencies += "javax.mail" % "mail" % "1.4"

libraryDependencies += "me.lessis" %% "courier" % "0.1.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

//libraryDependencies += "org.apache.commons" % "commons-dbcp2" % "2.0"

//libraryDependencies += "org.apache.commons" % "commons-pool2" % "2.0"

resolvers += "stepsoft" at "http://nexus.mcsherrylabs.com/nexus/content/groups/public"


