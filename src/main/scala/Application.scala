package com.htl.searchengine

import lucene.search.Engine
import dto.{Batch, JsonSupport, SearchResult}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import org.apache.logging.log4j.scala.Logging
import org.json4s._
import org.json4s.native.Serialization


import java.nio.file.Paths


object Application extends Logging with JsonSupport {

  implicit final val system: ActorSystem = ActorSystem("system")

  private implicit val formats = Serialization.formats(NoTypeHints)

  private val directoryPath = Paths.get(getClass.getResource("/d1").getPath)
  private val nGramPath = Paths.get(getClass.getResource("/d2").getPath)

  private val engine = new Engine(directoryPath, nGramPath, system)

  private def clearIndex(): Unit = {
    engine.clearIndex()
    logger.info(s"Index was cleared successfully")
  }

  private def processBatch(batch: Batch): Unit = {
    batch.documents.foreach(doc => {
      engine.addJsonDocument(doc)
    })
  }

  private def processQuery(field: String, query: String, top: String, maxFragmentSize: String): Array[SearchResult] = {
    engine.searchQuery(field, query, top.toInt, maxFragmentSize.toInt)
  }

  def main(args: Array[String]): Unit = {

    val route = {
        pathPrefix("engine") {
          concat(
              path("clear") {
                get {
                  clearIndex()
                  complete(StatusCodes.OK)
                }
            },
            get {
              parameters(Symbol("field"), Symbol("query"), Symbol("top"), Symbol("fragment")) {
                  (field: String, query: String, top: String, maxFragmentSize: String) =>
                val results = processQuery(field, query, top, maxFragmentSize)
                complete(HttpEntity(ContentTypes.`application/json`, Serialization.write(results)))
              }
            },
            post {
              entity(as[Batch]) { batch =>
                processBatch(batch)
                complete(StatusCodes.OK)
              }
            })
        }
    }

    val interface = "localhost"
    val port = 8081

    val _ = Http()
      .newServerAt(interface, port)
      .bind(route)


    logger.info(s"Server now online")

  }
}
