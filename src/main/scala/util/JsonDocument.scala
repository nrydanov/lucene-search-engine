package com.htl.searchengine
package util

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
