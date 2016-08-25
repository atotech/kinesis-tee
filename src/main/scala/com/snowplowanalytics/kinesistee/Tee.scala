package com.snowplowanalytics.kinesistee

import com.snowplowanalytics.kinesistee.filters.FilterStrategy
import com.snowplowanalytics.kinesistee.models.{Content, Stream}
import com.snowplowanalytics.kinesistee.routing.RoutingStrategy
import com.snowplowanalytics.kinesistee.transformation.TransformationStrategy

trait Tee {
  def tee(source: Stream,
          routingStrategy: RoutingStrategy,
          transformationStrategy: Option[TransformationStrategy],
          filterStrategy: Option[FilterStrategy],
          content: Seq[Content]): Unit
}
