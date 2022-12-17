package com.htl.searchengine
package dto

final case class SearchResult(title: String, fragment: String, url: String, score: Double) extends Serializable
