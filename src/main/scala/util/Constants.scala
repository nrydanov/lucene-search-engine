package com.htl.searchengine
package util

import scala.concurrent.duration.DurationInt

object Constants {
  final val BATCH_PROCESSING_RATE = 15.seconds
  final val BATCH_PROCESSING_DELAY = 0.seconds
  final val BATCH_PROCESSING_MESSAGE = "process"
}
