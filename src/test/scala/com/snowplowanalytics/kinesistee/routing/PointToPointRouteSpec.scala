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

package com.snowplowanalytics.kinesistee.routing

import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.snowplowanalytics.kinesistee.StreamWriter
import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.models.Stream
import org.specs2.mock.Mockito
import org.specs2.scalaz.ValidationMatchers

import scalaz.NonEmptyList

class PointToPointRouteSpec extends Specification with ValidationMatchers with Mockito {

  def buildStream(str:String): Stream = Stream(name = str)
  def buildTarget(str:String): StreamWriter = mock[StreamWriter]

  "point to point routing" should {

    "direct traffic from the given origin to the given destination" in {
      val target = buildTarget("destination")
      val route = new PointToPointRoute(buildStream("origin"), target)
      route.route(buildStream("origin")) must beSuccessful(target)
    }

    "reject routing from an unknown origin" in {
      val route = new PointToPointRoute(buildStream("gwen"), buildTarget("destination"))
      route.route(buildStream("origin")) must beFailing(NonEmptyList("Cannot route from origin 'origin', only origin 'gwen' is supported"))
    }

  }

  "rendering as a string" should {

    "display the source and destination" in {
      val dest = new StreamWriter(Stream("destination"), None, mock[AmazonKinesisClient])
      val sample = new PointToPointRoute(buildStream("source"), dest)
      sample.toString mustEqual s"Stream to stream route: stream `source` -> stream ${dest.toString}"
    }

  }

}
