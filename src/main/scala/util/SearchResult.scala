package com.htl.searchengine
package util

final case class SearchResult(title: String, fragment: String) extends Serializable {
  override def toString: String = {
    s"""
     {
       title: $title,
       fragment: $fragment
     }
     """
  }
}
