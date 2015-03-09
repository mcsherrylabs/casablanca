//import NativePackagerKeys._

//packageArchetype.java_application

scalaVersion := "2.10.4"

name := "demo_ui"

version := "1.0"

EclipseKeys.withSource := true

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.2"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.3.2"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies +=  "com.twitter" %% "finatra" % "1.6.0"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "com.stackmob" %% "newman" % "1.3.5"

libraryDependencies += "javax.mail" % "mail" % "1.4"

libraryDependencies += "me.lessis" %% "courier" % "0.1.3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "Twitter" at "http://maven.twttr.com"

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"
