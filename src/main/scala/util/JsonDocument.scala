package com.htl.searchengine
package util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class JsonDocument(title: String, body: String, categories: Array[String]) extends Serializable {

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

case class Batch(documents: List[JsonDocument])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonDocumentFormat: RootJsonFormat[JsonDocument] = jsonFormat3(JsonDocument)
  implicit val batchFormat: RootJsonFormat[Batch] = jsonFormat1(Batch) // contains List[Item]
}
