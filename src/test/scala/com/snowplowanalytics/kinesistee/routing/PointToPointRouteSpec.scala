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

import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.models.Stream
import org.specs2.scalaz.ValidationMatchers

import scalaz.NonEmptyList

class PointToPointRouteSpec extends Specification with ValidationMatchers {

  def buildStream(str:String): Stream = Stream(name = str)

  "point to point routing" should {

    "direct traffic from the given origin to the given destination" in {
      val route = new PointToPointRoute(buildStream("origin"), buildStream("destination"))
      route.route(buildStream("origin")) must beSuccessful(buildStream("destination"))
    }

    "reject routing from an unknown origin" in {
      val route = new PointToPointRoute(buildStream("gwen"), buildStream("destination"))
      route.route(buildStream("origin")) must beFailing(NonEmptyList("Cannot route from origin 'origin', only origin 'gwen' is supported"))
    }

  }

}
