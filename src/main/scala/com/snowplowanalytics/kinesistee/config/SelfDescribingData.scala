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

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.{Failure, Success, Try}


class SelfDescribingData(json: String) {

  implicit val defaultFormats = org.json4s.DefaultFormats

  private val parsed = Try(parse(json)) match {
    case Failure(f) => throw new IllegalArgumentException(s"Invalid self describing schema: invalid JSON Avro", f)
    case Success(s) => s
  }

  val schema: String = (parsed \\ "schema").extractOrElse[String](throw new IllegalArgumentException("Invalid self describing schema: missing the `schema` field"))
  val data: String =  parsed \ "data" match {
      case some:JObject => pretty(some)
      case _ => throw new IllegalArgumentException("Invalid self describing schema: missing the `data` field (or it is empty)")
  }

}
