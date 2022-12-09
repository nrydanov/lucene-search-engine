package com.htl.searchengine

import lucene.search.Engine
import util.{Batch, JsonSupport, SearchResult}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import org.apache.logging.log4j.scala.Logging

import java.nio.file.Paths

object Application extends Logging with JsonSupport {

  private val engine = new Engine(Paths.get(getClass.getResource("/directory").getPath))

  private def clearIndex(): Unit = {
    engine.clearIndex()
    logger.info("Index is cleared successfully")
  }

  private def resultsToString(results: Array[SearchResult]): String = {
    results.map(x => x.toString).mkString(",")
  }

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("system")

    val route = {
        pathPrefix("engine") {
          concat(
              path("clear") {
                get {
                  engine.clearIndex()
                  logger.info(s"Index was cleared successfully")
                  complete(StatusCodes.OK)
                }
            },
            get {
              parameters(Symbol("field"), Symbol("query"), Symbol("top")) { (field: String, query: String, top: String) =>
                val results = engine.searchQuery(field, query, top.toInt)
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, resultsToString(results)))

              }
            },
            post {
              entity(as[Batch]) { batch =>
                batch.documents.foreach(doc => {
                  engine.addJsonDocument(doc)
                  logger.info(s"Document ${doc.getTitle} was added")
                })

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
