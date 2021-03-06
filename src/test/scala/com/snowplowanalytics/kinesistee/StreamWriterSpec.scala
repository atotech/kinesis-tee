package com.snowplowanalytics.kinesistee

import java.io.{PrintWriter, StringWriter}
import java.nio.ByteBuffer
import java.util.concurrent.{Executor, TimeUnit}

import com.amazonaws.auth.{BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.kinesis.producer.{KinesisProducer, UserRecordResult}
import com.google.common.util.concurrent.ListenableFuture
import com.snowplowanalytics.kinesistee.config.TargetAccount
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.models._

class StreamWriterSpec extends Specification with Mockito {

  val sampleTarget = new TargetAccount("access_key", "secret_access_key", "eu-west-1")
  val sampleContent = Content("row", "p")

  "building a kinesis producer configuration" should {

    "set the region to that specified in the target account" in {
      val c = StreamWriter.buildProducerConfig(Some(sampleTarget))
      c.getRegion mustEqual "eu-west-1"
    }

    "set the region to null if the target account is not given" in {
      val c = StreamWriter.buildProducerConfig(None)
      c.getRegion mustEqual ""
    }

    "set the max connections to one" in {
      val c = StreamWriter.buildProducerConfig(None)
      c.getMaxConnections mustEqual 1
    }

    "set the max buffered time according to the constant value" in {
      val c = StreamWriter.buildProducerConfig(None)
      c.getRecordMaxBufferedTime mustEqual StreamWriter.MaxBufferedTime
    }

    "set the max timeout tie according to the constant value" in {
      val c = StreamWriter.buildProducerConfig(None)
      c.getRequestTimeout mustEqual StreamWriter.RequestTimeout
    }

    "use the account details for the target account, if one is given" in {
      val c = StreamWriter.buildProducerConfig(Some(sampleTarget))
      val creds = c.getCredentialsProvider.getCredentials
      val (key, secret) = (creds.getAWSAccessKeyId, creds.getAWSSecretKey)
      (key,secret) mustEqual ("access_key", "secret_access_key")
    }

    "use the default credentials provider chain if no target account is given" in {
      val c = StreamWriter.buildProducerConfig(None)
      c.getCredentialsProvider.isInstanceOf[DefaultAWSCredentialsProviderChain] mustEqual true
    }

  }

  "getting a kinesis producer" should {
    "return a KinesisProducer" in { StreamWriter.buildProducerConfig(None) must not beNull }
  }

  "formatting a stacktrace" should {

    "generate a sensible stacktrace" in {
      val sampleException = new IllegalStateException("hello")
      val sw = new StringWriter()
      sampleException.printStackTrace(new PrintWriter(sw))
      val expected = sw.toString

      StreamWriter.stacktrace(sampleException) mustEqual expected
    }

  }

  "flushing" should {

    "synchronously flush the kinesis producer" in {
      val producer = mock[KinesisProducer]
      new StreamWriter(Stream("sample"), None, producer).flush
      there was one (producer).flushSync()
    }

  }

  "closing a streamwriter" should {

    "flush the kinesis producer" in {
      val producer = mock[KinesisProducer]
      new StreamWriter(Stream("sample"), None, producer).close
      there was one (producer).flushSync()
    }

    "destroy the kinesis producer" in {
      val producer = mock[KinesisProducer]
      new StreamWriter(Stream("sample"), None, producer).close
      there was one (producer).destroy()
    }

  }

  "writing using a streamwriter" should {

    "add a record to the stream using the given producer" in {
      val producer = mock[KinesisProducer]

      producer.addUserRecord(any[String], any[String], any[ByteBuffer]) returns new ListenableFuture[UserRecordResult] {
        override def addListener(listener: Runnable, executor: Executor): Unit = {}

        override def isCancelled: Boolean = false

        override def get(): UserRecordResult = mock[UserRecordResult]

        override def get(timeout: Long, unit: TimeUnit): UserRecordResult = mock[UserRecordResult]

        override def cancel(mayInterruptIfRunning: Boolean): Boolean = false

        override def isDone: Boolean = true
      }

      new StreamWriter(Stream("sample"), None, producer).write(sampleContent)
      there was one (producer).addUserRecord("sample", sampleContent.partitionKey, ByteBuffer.wrap(sampleContent.row.getBytes("UTF-8")))
    }

  }

}
