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

import sbt._

object Dependencies {

  val snowplowRepo = "Snowplow Analytics" at "http://maven.snplow.com/releases/"

  object V {

    val jackson = "2.5.2"
    val json4s = "3.2.11"
    val scalaz = "7.1.1"
    val specs2 = "2.4"
    val scalazSpecs2 = "0.3.0"
    val configHocon = "1.3.0"
    val analyticsSdk = "0.1.0"

  }

  object Libraries {

    val specs2 = "org.specs2" %% "specs2-core" % V.specs2 % "test"
    val scalazSpecs2 = "org.typelevel" %% "scalaz-specs2" % V.scalazSpecs2 % "test"
    val specs2Mock = "org.specs2" %% "specs2-mock" % V.specs2 % "test"
    val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % V.specs2 % "test"
    val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz

    val jackson         = "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % V.jackson
    val json4s          = "org.json4s" %% "json4s-jackson" % V.json4s
    val json4sExt       = "org.json4s" %% "json4s-ext" % V.json4s
    val configHocon     = "com.typesafe" % "config" % V.configHocon

    val analyticsSdk = "com.snowplowanalytics" %% "snowplow-scala-analytics-sdk" % V.analyticsSdk

  }

}
