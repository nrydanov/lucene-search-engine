package com.htl.searchengine
package lucene.search

import lucene.index.InMemoryIndex
import util.JsonDocument

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.MMapDirectory

import java.io.File
import java.nio.file.Path

class Engine(path: Path) {

  private val directory = new MMapDirectory(path)

  private val analyzer = new RussianAnalyzer()

  private val index: InMemoryIndex = new InMemoryIndex(directory, analyzer)

  def searchOneTerm(term: Term, top: Int): Array[Document] = {

    val documents = index.searchIndex(new TermQuery(term), top)

    documents
  }

  def addJsonDocuments(path: String): Unit = {
    val d = new File(path)
    var files = Array[File]()

    if (d.exists && d.isDirectory) {
      files = d.listFiles.filter(file => file.isFile && file.getName.contains(".json"))
    }

    for (file <- files) {
      val doc = new JsonDocument(file.getAbsolutePath)
      index.indexDocument(doc.getTitle, doc.getBody, doc.getCategories)


    }
  }

  def clearIndex(): Unit = {
    index.clearIndex()
  }
}
