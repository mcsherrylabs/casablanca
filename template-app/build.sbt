import com.typesafe.sbt.packager.archetypes._


packagerSettings

JavaAppPackaging.settings

mappings in Universal += { file("bin/casablanca") -> "bin/casablanca" }

mappings in Universal += { file("conf/production-application.conf") -> "conf/production-application.conf" }

scalaVersion := "2.10.5"

version := "0.7.0"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "mcsherrylabs.com" %% "sss-casablanca" % "0.7.0-SNAPSHOT"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.3.2"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "javax.mail" % "mail" % "1.4"

libraryDependencies += "me.lessis" %% "courier" % "0.1.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "stepsoft" at "http://nexus.mcsherrylabs.com/nexus/content/groups/public"


