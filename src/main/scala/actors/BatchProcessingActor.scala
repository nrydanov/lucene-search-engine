package com.htl.searchengine
package actors

import dto.JsonDocument
import lucene.index.InMemoryIndex

import akka.actor.{Actor, Stash}
import org.apache.logging.log4j.scala.Logging

class BatchProcessingActor(index: InMemoryIndex) extends Actor with Logging with Stash {

  var batch = List.empty[JsonDocument]
  override def receive: Receive = {
    case "process" =>
      batch.foreach(json => {
        index.indexDocument(json.title, json.body, json.categories)
      })
      logger.info(s"${batch.length} were added since last job executed")
      batch = List.empty[JsonDocument]
    case json: JsonDocument =>
      batch = json :: batch
  }
}
