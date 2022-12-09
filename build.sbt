ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.4.0"
val LuceneVersion = "9.4.2"
val LuceneAnalyzerVersion = "8.11.2"
val Log4jVersion = "2.19.0"
val Log4jScalaVersion = "12.0"
val Sl4jVersion = "2.0.5"
val CirceVersion = "0.14.3"
val PlayVersion = "2.9.3"

lazy val root = (project in file("."))
  .settings(
    name := "SearchEngine",
    idePackagePrefix := Some("com.htl.searchengine")
  )

libraryDependencies += "com.typesafe.play" %% "play-json" % PlayVersion

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion
)

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % LuceneVersion,
  "org.apache.lucene" % "lucene-queryparser" % LuceneVersion,
  "org.apache.lucene" % "lucene-highlighter" % LuceneVersion,
  "org.apache.lucene" % "lucene-analyzers-common" % LuceneAnalyzerVersion
)

libraryDependencies ++= Seq(
  "org.apache.logging.log4j" % "log4j-core" % Log4jVersion,
  "org.apache.logging.log4j" %% "log4j-api-scala" % Log4jScalaVersion
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
)

libraryDependencies += "org.slf4j" % "slf4j-api" % Sl4jVersion
