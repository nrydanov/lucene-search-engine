package com.htl.searchengine
package dto

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
