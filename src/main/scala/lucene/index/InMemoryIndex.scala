package com.htl.searchengine
package lucene.index

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.{Document, Field, SortedDocValuesField, StoredField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, Sort}
import org.apache.lucene.store.Directory
import org.apache.lucene.util.BytesRef

class InMemoryIndex(var directory: Directory, var analyzer: Analyzer) {
  def indexDocument(title: String, body:String, categories: Array[String]): Unit = {
    val config = new IndexWriterConfig(analyzer)
    val writer = new IndexWriter(directory, config)
    val document = new Document()

    document.add(new SortedDocValuesField("title", new BytesRef(title)))
    document.add(new StoredField("title", title))
    document.add(new TextField("body", body, Field.Store.YES))
    document.add(new TextField("categories", categories.mkString(" "), Field.Store.NO))

    writer.addDocument(document)
    writer.close()
  }

  def searchIndex(query: Query, top: Int): Array[Document] = {
    val reader = DirectoryReader.open(directory)
    val searcher = new IndexSearcher(reader)
    val docs = searcher.search(query, top)

    docs.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
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
