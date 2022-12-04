ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "SearchEngine",
    idePackagePrefix := Some("com.htl.searchengine")
  )

libraryDependencies += "org.apache.lucene" % "lucene-core" % "9.4.2"
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "9.4.2"
libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "8.11.2"
libraryDependencies += "org.apache.lucene" % "lucene-highlighter" % "9.4.2"
libraryDependencies += "org.apache.lucene" % "lucene-spellchecker" % "3.6.2"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.3"

