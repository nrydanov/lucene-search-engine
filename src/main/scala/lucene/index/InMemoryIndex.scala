package com.htl.searchengine
package lucene.index

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.search.{IndexSearcher, Query}
import org.apache.lucene.store.Directory

class InMemoryIndex(var directory: Directory, var analyzer: Analyzer) {

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

    result
  }
}
