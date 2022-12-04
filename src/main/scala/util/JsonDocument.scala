package com.htl.searchengine
package util

import play.api.libs.json._

import scala.io.Source

class JsonDocument(filePath: String) {

  private val source = Source.fromFile(filePath)
  private val text = source.getLines.mkString
  source.close()

  private val json = Json.parse(text)

  private val title = (json \ "title").get.as[String]
  private val body = (json \ "body").get.as[String]
  private val categories = (json \ "categories").get.as[Array[String]]

  def getTitle: String = {
     title
  }

  def getBody: String = {
    body
  }

  def getCategories: Array[String] = {
    categories
  }
}
