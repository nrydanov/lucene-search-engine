package com.htl.searchengine
package lucene.search

import lucene.index.InMemoryIndex
import util.{JsonDocument, SearchResult}

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.highlight.{Highlighter, QueryScorer, SimpleHTMLFormatter, SimpleSpanFragmenter}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Path

class Engine(path: Path) {

  private final val MAX_FRAGMENT_SIZE = 100

  private val directory = new MMapDirectory(path)

  private val analyzer = new RussianAnalyzer()

  private val index: InMemoryIndex = new InMemoryIndex(directory, analyzer)

  private def convertToResult(documents: Array[Document], queryScorer: QueryScorer): Array[SearchResult] = {

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
    index.indexDocument(json.getTitle, json.getBody, json.getCategories)
  }

  def clearIndex(): Unit = {
    index.clearIndex()
  }
}
