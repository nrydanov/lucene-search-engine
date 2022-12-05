package com.htl.searchengine

import lucene.search.Engine

import org.apache.logging.log4j.scala.Logging
import org.apache.lucene.index.Term

import java.nio.file.Paths
import scala.io.StdIn.readLine

object Application extends Logging {

  private val e = new Engine(Paths.get(getClass.getResource("/directory").getPath))

  private def addDocuments(): Unit = {
    print("Enter folder name (from resources folder): ")

    val path = readLine()

    try {
      e.addJsonDocuments(getClass.getResource(path).getPath)
      logger.info("Documents was added successfully")
    }
    catch {
      case e: Exception => logger.error("Couldn't open folder")
    }
  }

  private def makeOneTermSearch(): Unit = {
    print("Enter field name: ")
    val field = readLine()

    print("Enter text: ")
    val text = readLine()

    print("Enter number of documents: ")
    val number = readLine().toInt

    val _ = e.searchOneTerm(new Term(field, text), number)
  }

  private def clearIndex(): Unit = {
    e.clearIndex()
    logger.info("Index is cleared successfully")
  }
  def main(args: Array[String]): Unit = {

    var active = true

    while (active) {
      println("Choose option:")
      println("1. Add documents from directory")
      println("2. Make one-term search")
      println("3. Clear index")

      print("Option: ")
      val query = readLine().toInt

      query match {
        case 1 => addDocuments()
        case 2 => makeOneTermSearch()
        case 3 => clearIndex()
        case _ => active = false
      }
    }
  }
}
