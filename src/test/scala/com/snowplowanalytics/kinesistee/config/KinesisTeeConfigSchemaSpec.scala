package com.snowplowanalytics.kinesistee.config

import java.io.{ByteArrayInputStream, PrintWriter, StringWriter}

import org.specs2.mutable.Specification
import com.sksamuel.avro4s.AvroInputStream
import com.snowplowanalytics.kinesistee.Configuration._


class KinesisTeeConfigSchemaSpec extends Specification {

  def stackTrace(e:Throwable) = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    sw.toString()
  }

  val sampleGoodConfig = scala.io.Source.fromURL(getClass.getResource("/sampleconfig.json")).mkString

  "load a sample configuration" in {
    val in = new ByteArrayInputStream(sampleGoodConfig.getBytes("UTF-8"))
    val input = AvroInputStream.json[Configuration](in)
    val result = input.singleEntity

    result match {
      case scala.util.Failure(f) => ko(stackTrace(f))
      case scala.util.Success(s) => s mustEqual Configuration(name = "My Kinesis Tee example",
                                                              sourceStream = SourceStream("my-source-stream", InitialPosition.TRIM_HORIZON, 10000),
                                                              targetStream = TargetStream("my-target-stream", None),
                                                              transformer = Transformer.SNOWPLOW_TO_JSON,
                                                              filter = None)
    }
  }

}
