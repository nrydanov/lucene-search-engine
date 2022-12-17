package com.htl.searchengine
package actors

import dto.JsonDocument
import lucene.index.InMemoryIndex
import util.Constants.BATCH_PROCESSING_MESSAGE

import akka.actor.{Actor, Stash}
import com.htl.searchengine.lucene.analyzer.NGramRussianAnalyzer
import org.apache.logging.log4j.scala.Logging

class BatchProcessingActor(index: InMemoryIndex, nGramIndex: InMemoryIndex) extends Actor with Logging with Stash {

  var batch = List.empty[JsonDocument]
  override def receive: Receive = {
    case BATCH_PROCESSING_MESSAGE =>
      batch.foreach(json => {
        index.indexDocument(json.title, json.body, json.categories, json.url)
        nGramIndex.indexDocument(json.title, json.body, json.categories, json.url)
      })
      logger.info(s"${batch.length} were added since last job executed")
      batch = List.empty[JsonDocument]
    case json: JsonDocument =>
      batch = json :: batch
  }
}
