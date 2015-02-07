
name := "casablanca"

version := "1.0"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.2"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.0.0"

libraryDependencies += "com.typesafe" % "config" % "1.2.1"
