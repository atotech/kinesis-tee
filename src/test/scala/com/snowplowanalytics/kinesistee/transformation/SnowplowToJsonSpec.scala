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

package com.snowplowanalytics.kinesistee.transformation

import com.snowplowanalytics.kinesistee.models.Content
import org.json4s.JValue
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class SnowplowToJsonSpec extends Specification with ValidationMatchers {


  def enrichedEvent(schema: String, unstructJson: JValue): String = {
    val unstruct =
      ("schema" -> "iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0") ~
        ("data" -> ("schema" -> schema) ~
          ("data" -> unstructJson))

    val sampleEnrichedRowFormatString =
      """com.snowplowanalytics.acme pc 2016-02-23 15:26:24.473 2016-02-23 15:26:24.295 2016-02-23 15:25:35.898 unstruct 5627723d-0f33-44d6-80a7-3e43b05e49f9 mytrackername scala-0.2.0 ssc-0.5.0-kinesis kinesis-0.6.0-common-0.15.0 sample-user 86.30.71.226 1b78b840-57af-40ae-9407-2abcf4da0d53 %s spray-can/1.3.3 2016-02-23 15:25:35.898"""
    sampleEnrichedRowFormatString.format(compact(render(unstruct)))
  }


  "converting a Snowplow enriched event to JSON" should {

    "convert a valid snowplow event to JSON" in {

      val payload =
        """
          |{
          |   "targetUrl": "http://www.example.com",
          |   "elementClasses": ["foreground"],
          |   "elementId": "exampleLink"
          |}
        """.stripMargin

      val sampleGoodEvent = enrichedEvent("""iglu:com.snowplowanalytics.snowplow/link_click/jsonschema/1-0-1""", parse(payload))

      new SnowplowToJson().transform(Content(sampleGoodEvent)) must beSuccessful
    }

  }

}
