package com.snowplowanalytics.kinesistee

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.StreamWriter
import com.snowplowanalytics.kinesistee.filters.FilterStrategy
import com.snowplowanalytics.kinesistee.models.Content
import com.snowplowanalytics.kinesistee.models.Stream
import com.snowplowanalytics.kinesistee.transformation.TransformationStrategy
import org.mockito.Matchers.{eq => eqTo}

import scalaz.syntax.validation._
import scalaz.ValidationNel

class MainSpec extends Specification with Mockito {

  "the tee function" should {

    "write everything to the StreamWriter if no filter strategy is in use" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))
      val mockWriter = mock[StreamWriter]
      Main.tee(Stream("sample"), mockWriter, None, None, sampleContent)
      there was three (mockWriter).write(eqTo(Content("a")))
    }

    "write to the stream writer only if the filter function returns false" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))
      val mockWriter = mock[StreamWriter]

      class FilterEverything extends FilterStrategy {
        override def filter(origin: Stream, content: Content): ValidationNel[Throwable, Boolean] = {
          true.success
        }
      }

      Main.tee(source = Stream("sample"),
               target = mockWriter,
               transformationStrategy = None,
               filterStrategy = Some(new FilterEverything),
               content = sampleContent)
      there was no (mockWriter).write(any[Content])
    }

    "transform stream content using the given transformation strategy" in {
      val sampleContent = Seq(Content("a"), Content("a"), Content("a"))

      class MakeEverythingB extends TransformationStrategy {
        override def transform(content: Content): ValidationNel[Throwable, Content] = {
          Content("b").success
        }
      }

      val streamWriter = mock[StreamWriter]
      Main.tee(Stream("sample"), streamWriter, Some(new MakeEverythingB), None, sampleContent)

      there was three (streamWriter).write(eqTo(Content("b")))
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

      val streamWriter = mock[StreamWriter]
      Main.tee(Stream("sample"), streamWriter, Some(new MakeEverythingB), Some(new FilterNotB), sampleContent)

      there was three (streamWriter).write(eqTo(Content("b")))
    }

    "throw failures in the filter strategy before pushing anything to the stream writer" in {
      // come back to me
      ko
    }

    "throw failures in the transformation strategy before pushing anything to the stream writer" in {
      ko
    }

  }
}
