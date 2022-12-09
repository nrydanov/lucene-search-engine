package com.htl.searchengine

import lucene.search.Engine
import util.{JsonDocument, SearchResult}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import org.apache.logging.log4j.scala.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{Json, Reads}

import java.nio.file.Paths
import java.util.concurrent.ConcurrentLinkedQueue

object Application extends Logging {

  final case class Documents(messages: List[String])

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

    val batch = new ConcurrentLinkedQueue[String]

    val route = {
        path("engine") {
          concat(
            get {
              parameters(Symbol("field"), Symbol("query"), Symbol("top")) { (field: String, query: String, top: String) =>
                val results = engine.searchQuery(field, query, top.toInt)
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, resultsToString(results)))
              }
            },
            post {
              entity(as[String]) { jsonString =>
                implicit val reader: Reads[JsonDocument] = Json.reads[JsonDocument]

                try {
                  engine.addJsonDocument(reader.reads(Json.toJson(jsonString)).get)
                  logger.info(s"Document was indexed")
                  complete(StatusCodes.OK)
                }
                catch {
                  case e: Throwable =>
                    logger.error(e.getMessage)
                    complete(StatusCodes.BadRequest)
                }
              }
            })
        }
    }

    val interface = "localhost"
    val port = 8081

    val _ = Http().newServerAt(interface, port).bind(route)

    logger.info(s"Server now online")

  }
}
