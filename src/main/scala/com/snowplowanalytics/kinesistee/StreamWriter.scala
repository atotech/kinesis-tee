package com.snowplowanalytics.kinesistee

import com.snowplowanalytics.kinesistee.config.TargetAccount
import com.snowplowanalytics.kinesistee.models.{Content, Stream}

class StreamWriter(stream: Stream, targetAccount: Option[TargetAccount]) {

  def write(content: Content): Unit = {
      // come back to this
      // put record on KPL list
  }

  def flush: Unit = {
    // come back to this
    // push through
    // KPL -> flushSync
  }

  override def toString: String = {
      s"`${stream.name}`, using separate account details: " + (if (targetAccount.isDefined) { "TRUE" } else { "FALSE" })
  }

}
