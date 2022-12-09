package com.htl.searchengine
package lucene.search

import Application.system.dispatcher
import actors.BatchProcessingActor
import dto.{JsonDocument, SearchResult}
import lucene.index.InMemoryIndex
import util.Constants.{BATCH_PROCESSING_DELAY, BATCH_PROCESSING_MESSAGE, BATCH_PROCESSING_RATE}

import akka.actor.{ActorSystem, Props}
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.highlight.{Highlighter, QueryScorer, SimpleHTMLFormatter, SimpleSpanFragmenter}
import org.apache.lucene.search.{BooleanQuery, FuzzyQuery}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Path
import scala.io.Source

class Engine(directoryPath: Path, actorSystem: ActorSystem) {

  private val directory = new MMapDirectory(directoryPath)

  private val analyzer = new RussianAnalyzer()

  private val index: InMemoryIndex = new InMemoryIndex(directory, analyzer)

  private val batchProcessor = actorSystem.actorOf(Props(classOf[BatchProcessingActor], index),
    "batch-processor-actor")

  actorSystem.scheduler.scheduleAtFixedRate(BATCH_PROCESSING_DELAY, BATCH_PROCESSING_RATE,
    batchProcessor, BATCH_PROCESSING_MESSAGE)

  private def convertToResult(documents: IndexedSeq[Document], queryScorer: QueryScorer,
                              maxFragmentSize: Int): Array[SearchResult] = {

    val highlighter = new Highlighter(new SimpleHTMLFormatter(), queryScorer)

    highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, maxFragmentSize))
    highlighter.setMaxDocCharsToAnalyze(Int.MaxValue)

    val results = new Array[SearchResult](documents.length)

    for (i <- results.indices) {
      val document = documents(i)
      val fragment = highlighter.getBestFragment(this.analyzer, "body", document.get("body"))
      results(i) = SearchResult(document.get("title"), fragment)
    }

    results
  }

  def findSynonyms(word: String): Array[String] = {
    new Array[String](0)
  }

  def searchQuery(field: String, queryString: String, top: Int, maxFragmentSize: Int): Array[SearchResult] = {

    val query = new QueryParser(field, analyzer).parse(queryString)
    val results = index.searchIndex(query, top)

    if (results.length < top) {
      val tokenStream = analyzer.tokenStream(field, queryString)
      val booleanQueryBuilder = new BooleanQuery.Builder().add(query, Occur.SHOULD)

      tokenStream.reset()
      do {
        val token = tokenStream.getAttribute(classOf[CharTermAttribute]).toString
        booleanQueryBuilder.add(new FuzzyQuery(new Term(field, token)), Occur.SHOULD)

      } while (tokenStream.incrementToken())
      tokenStream.reset()

      tokenStream.close()

      val booleanQuery = booleanQueryBuilder.build()

      convertToResult(results.concat(index.searchIndex(booleanQuery, top)), new QueryScorer(booleanQuery), maxFragmentSize)
    } else {
      convertToResult(results, new QueryScorer(query), maxFragmentSize)
    }
  }

  def addJsonDocument(json: JsonDocument): Unit = {
    batchProcessor ! json
  }

  def clearIndex(): Unit = {
    index.clearIndex()
  }
}
