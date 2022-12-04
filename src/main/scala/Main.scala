package com.htl.searchengine

import lucene.search.Engine

import org.apache.lucene.index.Term

import java.nio.file.Paths

object Main {
  def main(args: Array[String]): Unit = {
    val e = new Engine(Paths.get(getClass.getResource("/directory").getPath))
    e.addJsonDocuments(getClass.getResource("/scrapped").getPath)

    val _ = e.searchOneTerm(new Term("body", "азерит"), 10)
  }
}
