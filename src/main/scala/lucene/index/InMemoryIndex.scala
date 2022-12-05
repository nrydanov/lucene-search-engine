package com.htl.searchengine
package lucene.index

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.{Document, Field, SortedDocValuesField, StoredField, TextField}
import org.apache.lucene.document._
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.highlight.{Highlighter, QueryScorer, SimpleHTMLFormatter, SimpleSpanFragmenter}
import org.apache.lucene.search.{IndexSearcher, Query, Sort}
import org.apache.lucene.store.Directory
import org.apache.lucene.util.BytesRef


class InMemoryIndex(var directory: Directory, var analyzer: Analyzer) {

  private final val MAX_FRAGMENT_SIZE = 100

  def indexDocument(title: String, body:String, categories: Array[String]): Unit = {
    val config = new IndexWriterConfig(analyzer)
    val writer = new IndexWriter(directory, config)
    val document = new Document()

    document.add(new TextField("title", title, Field.Store.YES))
    document.add(new TextField("body", body, Field.Store.YES))
    categories.foreach(cat => document.add(new TextField("categories", cat, Field.Store.YES)))

    writer.addDocument(document)
    writer.close()
  }

  def clearIndex(): Unit = {

    val config = new IndexWriterConfig(analyzer).setOpenMode(OpenMode.CREATE)
    val writer = new IndexWriter(directory, config)

    writer.close()
  }

  def searchIndex(query: Query, top: Int): Array[Document] = {
    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val docs = searcher.search(query, top)

    val result = docs.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
    val queryScorer = new QueryScorer(query)

    val highlighter = new Highlighter(new SimpleHTMLFormatter(), queryScorer)

    highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, this.MAX_FRAGMENT_SIZE))
    highlighter.setMaxDocCharsToAnalyze(Int.MaxValue)

    for (document <- result) {
      println(s"Title: ${document.get("title")}\n")
      val fragment = highlighter.getBestFragment(this.analyzer, "body", document.get("body"))
      println(s"Fragment: ...$fragment...\n==========================\n")
    }

    result
  }

  private def searchIndex(query: Query, top: Int, sort: Sort): Array[Document] = {
    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val docs = searcher.search(query, top, sort)
    docs.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
  }

  def searchIndex(fieldName: String, queryString: String, top: Int): Array[Document] = {
    val query = new QueryParser(fieldName, analyzer).parse(queryString)

    searchIndex(query, top)
  }
}
