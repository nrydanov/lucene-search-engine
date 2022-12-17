package com.htl.searchengine
package lucene.analyzer

import org.apache.lucene.analysis.snowball.SnowballFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis._
import org.apache.lucene.analysis.shingle.ShingleFilter
import org.apache.lucene.util.IOUtils
import org.tartarus.snowball.ext.RussianStemmer

import java.nio.charset.StandardCharsets

class NGramRussianAnalyzer extends Analyzer {

  private final val DEFAULT_STOPWORD_FILE = "russian_stop.txt"

  private final val DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(
    IOUtils.getDecodingReader(classOf[SnowballFilter], DEFAULT_STOPWORD_FILE, StandardCharsets.UTF_8))
  override def createComponents(fieldName: String): Analyzer.TokenStreamComponents = {
    val source = new StandardTokenizer
    var result: TokenStream = new LowerCaseFilter(source)
    result = new StopFilter(result, DEFAULT_STOP_SET)
    result = new SnowballFilter(result, new RussianStemmer)
    result = new ShingleFilter(result, 2, 3)
    new Analyzer.TokenStreamComponents(source, result)
  }
}
