package com.htl.searchengine

import lucene.search.Engine

import org.apache.lucene.index.Term
import org.apache.lucene.search.highlight.{GradientFormatter, Highlighter, QueryScorer}

import java.nio.file.Paths

object Main {
  def main(args: Array[String]): Unit = {
    val e = new Engine(Paths.get(getClass.getResource("/directory").getPath))
    e.addJsonDocuments(getClass.getResource("/scrapped").getPath)

    val scoreDocs = e.searchOneTerm(new Term("body", "азерит"))

    for (doc <- scoreDocs) {
      val title = doc.get("title")
      val

      println(title)
    }
    print("Done")
  }
}