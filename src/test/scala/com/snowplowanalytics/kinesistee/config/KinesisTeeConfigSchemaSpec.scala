/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.snowplowanalytics.kinesistee.config

import java.io.{ByteArrayInputStream, PrintWriter, StringWriter}

import org.specs2.mutable.Specification
import com.sksamuel.avro4s.AvroInputStream


class KinesisTeeConfigSchemaSpec extends Specification {

  def stackTrace(e:Throwable) = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    sw.toString()
  }

  val sampleGoodConfig = scala.io.Source.fromURL(getClass.getResource("/sampleconfig.json")).mkString

  "load a sample configuration" in {
    val in = new ByteArrayInputStream(sampleGoodConfig.getBytes("UTF-8"))
    val input = AvroInputStream.json[Configuration](in)
    val result = input.singleEntity

    result match {
      case scala.util.Failure(f) => ko(stackTrace(f))
      case scala.util.Success(s) => s mustEqual Configuration(name = "My Kinesis Tee example",
                                                              sourceStream = SourceStream("my-source-stream", InitialPosition.TRIM_HORIZON, 10000),
                                                              targetStream = TargetStream("my-target-stream", None),
                                                              transformer = Transformer.SNOWPLOW_TO_JSON,
                                                              filter = None)
    }
  }

}
