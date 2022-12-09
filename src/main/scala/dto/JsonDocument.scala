package com.htl.searchengine
package dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.util

final case class JsonDocument(title: String, body: String, categories: Array[String]) extends Serializable

case class Batch(documents: List[JsonDocument])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonDocumentFormat: RootJsonFormat[JsonDocument] = jsonFormat3(JsonDocument)
  implicit val batchFormat: RootJsonFormat[Batch] = jsonFormat1(Batch)
}
