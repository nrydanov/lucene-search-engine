package com.htl.searchengine
package lucene.search

import Application.system.dispatcher
import actors.BatchProcessingActor
import dto.{JsonDocument, SearchResult}
import lucene.index.InMemoryIndex

import akka.actor.{ActorSystem, Props}
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.highlight.{Highlighter, QueryScorer, SimpleHTMLFormatter, SimpleSpanFragmenter}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Path
import scala.concurrent.duration.DurationInt

class Engine(path: Path, actorSystem: ActorSystem) {

  private final val MAX_FRAGMENT_SIZE = 100

  private val directory = new MMapDirectory(path)

  private val analyzer = new RussianAnalyzer()

  private val index: InMemoryIndex = new InMemoryIndex(directory, analyzer)

  private val batchProcessor = actorSystem.actorOf(Props(classOf[BatchProcessingActor], index), "batch-processor-actor")

  actorSystem.scheduler.scheduleAtFixedRate(0.seconds, 5.seconds, batchProcessor, "process")
  private def convertToResult(documents: IndexedSeq[Document], queryScorer: QueryScorer): Array[SearchResult] = {

    val highlighter = new Highlighter(new SimpleHTMLFormatter(), queryScorer)

    highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, this.MAX_FRAGMENT_SIZE))
    highlighter.setMaxDocCharsToAnalyze(Int.MaxValue)

    val results = new Array[SearchResult](documents.length)

    for (i <- results.indices) {
      val document = documents(i)
      val fragment = highlighter.getBestFragment(this.analyzer, "body", document.get("body"))
      results(i) = SearchResult(document.get("title"), fragment)
    }

    results
  }

  def searchQuery(field: String, queryString: String, top: Int): Array[SearchResult] = {
    val query = new QueryParser(field, analyzer).parse(queryString)
    val results = convertToResult(index.searchIndex(query, top), new QueryScorer(query))

    results
  }

  def addJsonDocument(json: JsonDocument): Unit = {
    batchProcessor ! json
  }

  def clearIndex(): Unit = {
    index.clearIndex()
  }
}
