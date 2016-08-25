package com.snowplowanalytics.kinesistee

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.filters.FilterStrategy
import com.snowplowanalytics.kinesistee.models.Content
import com.snowplowanalytics.kinesistee.models.Stream
import com.snowplowanalytics.kinesistee.routing.RoutingStrategy
import com.snowplowanalytics.kinesistee.transformation.TransformationStrategy
import org.mockito.Matchers.{eq => eqTo}

import scalaz.syntax.validation._
import scalaz.ValidationNel

class KinesisTeeSpec extends Specification with Mockito {

  "the tee function" should {

    def mockRoute = new RoutingStrategy {
      val mockStreamWriter = mock[StreamWriter]
      override def route(origin: Stream): ValidationNel[String, StreamWriter] = mockStreamWriter.success
    }

    "write everything to the StreamWriter if no filter strategy is in use" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))
      val route = mockRoute
      KinesisTee.tee(Stream("sample"), route, None, None, sampleContent)
      there was three (route.mockStreamWriter).write(eqTo(Content("a")))
    }

    "write to the stream writer only if the filter function returns false" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))

      class FilterEverything extends FilterStrategy {
        override def filter(origin: Stream, content: Content): ValidationNel[Throwable, Boolean] = {
          true.success
        }
      }

      val routeMock = mockRoute
      KinesisTee.tee(source = Stream("sample"),
                     routingStrategy = routeMock,
                     transformationStrategy = None,
                     filterStrategy = Some(new FilterEverything),
                     content = sampleContent)
      there was no (routeMock.mockStreamWriter).write(any[Content])
    }

    "transform stream content using the given transformation strategy" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))

      class MakeEverythingB extends TransformationStrategy {
        override def transform(content: Content): ValidationNel[Throwable, Content] = {
          Content("b").success
        }
      }

      val routeMock = mockRoute
      KinesisTee.tee(Stream("sample"), routeMock, Some(new MakeEverythingB), None, sampleContent)

      there was three (routeMock.mockStreamWriter).write(eqTo(Content("b")))
    }

    "run the transformation strategy prior to the filter strategy" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))

      class MakeEverythingB extends TransformationStrategy {
        override def transform(content: Content): ValidationNel[Throwable, Content] = {
          Content("b").success
        }
      }

      class FilterNotB extends FilterStrategy {
        override def filter(origin: Stream, content: Content): ValidationNel[Throwable, Boolean] = {
          content match {
            case Content("b") => false.success
            case _ => true.success
          }
        }
      }

      val routeMock = mockRoute
      KinesisTee.tee(Stream("sample"), routeMock, Some(new MakeEverythingB), Some(new FilterNotB), sampleContent)

      there was three (routeMock.mockStreamWriter).write(eqTo(Content("b")))
    }

    "throw failures in the filter strategy before pushing anything to the stream writer" in {
      class FailureFilter extends FilterStrategy {
        override def filter(origin: Stream, content: Content): ValidationNel[Throwable, Boolean] = new IllegalArgumentException("something").failureNel
      }

      val routeMock = mockRoute
      KinesisTee.tee(Stream("sample"), routeMock, None, Some(new FailureFilter()), Seq(Content("b"))) must throwA[IllegalStateException]
      there was no (routeMock.mockStreamWriter).write(any[Content])
    }

    "throw failures in the transformation strategy before pushing anything to the stream writer" in {
      class FailureTransform extends TransformationStrategy {
        override def transform(content: Content): ValidationNel[Throwable, Content] = new IllegalStateException("something").failureNel
      }

      val routeMock = mockRoute
      KinesisTee.tee(Stream("sample"), routeMock, Some(new FailureTransform), None, Seq(Content("b"))) must throwA[IllegalStateException]
      there was no (routeMock.mockStreamWriter).write(any[Content])
    }

  }
}
