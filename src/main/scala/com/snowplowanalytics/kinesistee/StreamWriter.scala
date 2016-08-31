package com.snowplowanalytics.kinesistee

import java.io.{PrintWriter, StringWriter}
import java.nio.ByteBuffer

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.producer._
import com.snowplowanalytics.kinesistee.config.TargetAccount
import com.snowplowanalytics.kinesistee.models.{Content, Stream}
import com.google.common.collect.Iterables
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import scala.collection.JavaConverters._

class StreamWriter(stream: Stream, targetAccount: Option[TargetAccount], producer: KinesisProducer) {

  val future = new FutureCallback[UserRecordResult]() {
    override def onFailure(t: Throwable) = {

      val last = t match {
        case failure: UserRecordFailedException => Some(Iterables.getLast(failure.getResult.getAttempts))
        case _ => None
      }


      if (last.isDefined) {
        System.err.println(s"Record failed to put - ${last.get.getErrorCode} : ${last.get.getErrorMessage}\nCaused by:\n${StreamWriter.stacktrace(t)}")
      } else {
        System.err.println(s"Record failed to put!\nCaused by:\n${StreamWriter.stacktrace(t)}")
      }
    }

    override def onSuccess(result: UserRecordResult) {}
  }


  def write(content: Content): Unit = {
    // put record on KPL list
    val r: ListenableFuture[UserRecordResult] = producer.addUserRecord(stream.name, content.partitionKey, ByteBuffer.wrap(content.row.getBytes("UTF-8")))
    Futures.addCallback(r, future)
  }

  def flush: Unit = {
    // come back to this
    // push through
    // KPL -> flushSync
    producer.flushSync()
  }

  def close: Unit = {
    flush
    producer.destroy()
  }

  override def toString: String = {
      s"`${stream.name}`, using separate account details: " + (if (targetAccount.isDefined) { "TRUE" } else { "FALSE" })
  }

}

object StreamWriter {

  val MaxBufferedTime = 1000
  val RequestTimeout = 5000

  def stacktrace(t:Throwable): String = {
    val sw = new StringWriter()
    t.printStackTrace(new PrintWriter(sw))
    sw.toString
  }

  def buildProducerConfig(targetAccount: Option[TargetAccount]) = {
    val region = targetAccount match {
      case Some(t) => Some(t.region)
      case None => None
    }

    val producerConfig = new KinesisProducerConfiguration()
      .setMaxConnections(1)
      .setRecordMaxBufferedTime(MaxBufferedTime)
      .setRequestTimeout(RequestTimeout)

    if (region.isDefined) {
      producerConfig.setRegion(region.get)
    }

    producerConfig.setCredentialsProvider(

      if (targetAccount.isDefined) {
        val acct = targetAccount.get
        new AWSCredentialsProvider() {
          override def refresh(): Unit = {}
          override def getCredentials: AWSCredentials = new BasicAWSCredentials(acct.awsAccessKey, acct.awsSecretAccessKey)
        }
      } else {
        new DefaultAWSCredentialsProviderChain()
      }

    )
  }

  def buildProducer(targetAccount: Option[TargetAccount]) = {
    new KinesisProducer(buildProducerConfig(targetAccount))
  }

}
