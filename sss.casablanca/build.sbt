
scalaVersion := "2.10.5"

name := "sss-casablanca"

version := "0.7.0"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.2"

libraryDependencies += "mcsherrylabs.com" %% "sss-db" % "0.9"

libraryDependencies += "mcsherrylabs.com" %% "sss-ancillary" % "0.9"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.3.2"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.twitter" % "finatra_2.10" % "1.6.0"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "com.stackmob" %% "newman" % "1.3.5"

libraryDependencies += "javax.mail" % "mail" % "1.4"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

//libraryDependencies += "org.apache.commons" % "commons-dbcp2" % "2.0"

resolvers += "Twitter" at "http://maven.twttr.com"

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

resolvers += "stepsoft" at "http://nexus.mcsherrylabs.com/nexus/content/groups/public"
