package com.htl.searchengine
package actors

import dto.{Batch, JsonDocument}
import lucene.index.InMemoryIndex

import akka.actor.{Actor, ActorRef, Props, Stash}
import org.apache.logging.log4j.scala.Logging

import java.util
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

class BatchProcessingActor(index: InMemoryIndex) extends Actor with Logging with Stash {

  val batch = new util.LinkedList[JsonDocument]
  override def receive: Receive = {
    case "process" =>
      for (json <- batch) {
        index.indexDocument(json.title, json.body, json.categories)
        logger.info(s"${batch.size} were added since last job executed")
        batch.clear()
      }
    case json: JsonDocument =>
      batch.add(json)
  }
}
