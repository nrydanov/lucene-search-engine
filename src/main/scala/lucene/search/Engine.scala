package com.htl.searchengine
package lucene.search

import Application.system.dispatcher
import actors.BatchProcessingActor
import dto.{JsonDocument, SearchResult}
import lucene.index.InMemoryIndex
import util.Constants.{BATCH_PROCESSING_DELAY, BATCH_PROCESSING_MESSAGE, BATCH_PROCESSING_RATE}

import akka.actor.{ActorSystem, Props}
import com.htl.searchengine.lucene.analyzer.NGramRussianAnalyzer
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.highlight.{Highlighter, QueryScorer, SimpleHTMLFormatter, SimpleSpanFragmenter}
import org.apache.lucene.search.{BooleanQuery, BoostQuery, FuzzyQuery, PhraseQuery}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Path
import scala.io.Source

class Engine(directoryPath: Path, nGramDirectoryPath: Path, actorSystem: ActorSystem) {

  private val directory = new MMapDirectory(directoryPath)
  private val nGramDirectory = new MMapDirectory(nGramDirectoryPath)

  private val analyzer = new RussianAnalyzer()
  private val nGramAnalyzer = new NGramRussianAnalyzer()

  private val index: InMemoryIndex = new InMemoryIndex(directory, analyzer)
  private val nGramIndex: InMemoryIndex = new InMemoryIndex(nGramDirectory, nGramAnalyzer)

  private val batchProcessor = actorSystem.actorOf(Props(classOf[BatchProcessingActor], index, nGramIndex),
    "batch-processor-actor")

  actorSystem.scheduler.scheduleAtFixedRate(BATCH_PROCESSING_DELAY, BATCH_PROCESSING_RATE,
    batchProcessor, BATCH_PROCESSING_MESSAGE)

  private def convertToResult(documents: IndexedSeq[(Double, Document)], queryScorer: QueryScorer,
                              maxFragmentSize: Int): Array[SearchResult] = {

    val highlighter = new Highlighter(new SimpleHTMLFormatter(), queryScorer)

    highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, maxFragmentSize))
    highlighter.setMaxDocCharsToAnalyze(Int.MaxValue)

    val results = new Array[SearchResult](documents.length)

    for (i <- results.indices) {
      val entry = documents(i)
      val fragment = highlighter.getBestFragment(this.analyzer, "body", entry._2.get("body"))
      results(i) = SearchResult(entry._2.get("title"), fragment.replace("\"", "\'")
        .replace("\n", "\\n"), entry._2.get("url"), entry._1)
    }

    results
  }

  private def normalizeResult(results: Array[(Float, Document)], factor: Double = 1) = {
    var bestRank = 0.0
    for (result <- results) {
      bestRank = Math.max(result._1, bestRank)
    }

    results.map(pair => ((pair._1 / bestRank) * factor, pair._2))
  }

  def searchQuery(field: String, queryString: String, top: Int, maxFragmentSize: Int): Array[SearchResult] = {

    val query = new QueryParser(field, analyzer).parse(queryString)
    val nGramQuery = new QueryParser(field, nGramAnalyzer).parse(queryString)
    val phraseQuery = new PhraseQuery(field, queryString)
    val booleanQuery = new BooleanQuery.Builder()
      .add(new BoostQuery(query, 0.6f), Occur.SHOULD)
      .add(new BoostQuery(phraseQuery, 0.9f), Occur.SHOULD)
      .build()

    val nGramTop = normalizeResult(index.searchIndex(nGramQuery, top))
    val booleanQueryTop = normalizeResult(index.searchIndex(booleanQuery, top), 0.95)

    var results = Array.concat(nGramTop, booleanQueryTop)

    if (results.length < top) {
      val tokenStream = analyzer.tokenStream(field, queryString)
      val booleanQueryBuilder = new BooleanQuery.Builder().add(booleanQuery, Occur.SHOULD)

      tokenStream.reset()
      do {
        val token = tokenStream.getAttribute(classOf[CharTermAttribute]).toString
        booleanQueryBuilder.add(new FuzzyQuery(new Term(field, token)), Occur.SHOULD)

      } while (tokenStream.incrementToken())
      tokenStream.reset()
      tokenStream.close()

      val fuzzyBooleanQuery = booleanQueryBuilder.build()

      val additionalQueryResults = normalizeResult(index.searchIndex(fuzzyBooleanQuery, top), 0.9)
      results = results.concat(additionalQueryResults)

      results = results.sortBy(-_._1).distinctBy(_._1)
      convertToResult(results, new QueryScorer(fuzzyBooleanQuery), maxFragmentSize)
    } else {
      results = results.sortBy(-_._1).distinctBy(_._1)
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
