package com.snowplowanalytics.kinesistee

import com.snowplowanalytics.kinesistee.config.TargetAccount
import com.snowplowanalytics.kinesistee.models.{Content, Stream}

class StreamWriter(stream: Stream, targetAccount: Option[TargetAccount]) {

  def write(content: Content): Unit = {
      // come back to this
  }

}
