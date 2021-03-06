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
import com.snowplowanalytics.snowplow.analytics.scalasdk.json.EventTransformer

import scalaz.{Failure, Success, ValidationNel}
import scalaz.syntax.validation._

class SnowplowToJson extends TransformationStrategy {

  override def transform(content: Content): ValidationNel[Throwable, Content] = {
    EventTransformer.transform(content.row) match {
      case Success(s) => Content(s, content.partitionKey).success
      case Failure(f) => new IllegalArgumentException(f.head.toString).failureNel
    }
  }

}
