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

import org.specs2.mutable.Specification

class ConfigurationBuilderSpec extends Specification {

  "A valid configuration" should {

    val sampleGoodConfig = scala.io.Source.fromURL(getClass.getResource("/sample_self_describing_config.json")).mkString

    "generate the correct case class" in {
      ConfigurationBuilder.build(sampleGoodConfig) mustEqual Configuration(name = "My Kinesis Tee example",
                                                                           sourceStream = SourceStream("my-source-stream", InitialPosition.TRIM_HORIZON, 10000),
                                                                           targetStream = TargetStream("my-target-stream", None),
                                                                           transformer = Transformer.SNOWPLOW_TO_JSON,
                                                                           filter = None)
    }


  }

  "An invalid JSON configuration" should {

    "throw an exception" in {
      ConfigurationBuilder.build("banana") must throwA[IllegalArgumentException]
    }

  }

  "A configuration that doesn't match the given schema" should {

    "throw an exception" in {
      ConfigurationBuilder.build(
        """
          |{
          |  "schema": "com.thing",
          |  "data": { "foo":"bar" }
          |}
        """.stripMargin) must throwA(new IllegalArgumentException("Invalid configuration"))
    }

  }

}
