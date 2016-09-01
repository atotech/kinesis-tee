package com.snowplowanalytics.kinesistee

import java.io.{PrintWriter, StringWriter}
import java.nio.ByteBuffer

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.snowplowanalytics.kinesistee.config.TargetAccount
import com.snowplowanalytics.kinesistee.models.{Content, Stream}

class StreamWriter(stream: Stream, targetAccount: Option[TargetAccount], producer: AmazonKinesisClient) {

  def write(content: Content): Unit = {
    producer.putRecord(stream.name, ByteBuffer.wrap(content.row.getBytes("UTF-8")), content.partitionKey)
  }

  def flush: Unit = {
  }

  def close: Unit = {
    flush
  }

  override def toString: String = {
      s"`${stream.name}`, using separate account details: " + (if (targetAccount.isDefined) { "TRUE" } else { "FALSE" })
  }

}

object StreamWriter {

  def stacktrace(t:Throwable): String = {
    val sw = new StringWriter()
    t.printStackTrace(new PrintWriter(sw))
    sw.toString
  }

  def buildClientConfig(targetAccount: Option[TargetAccount]) = {

    val credentialsProvider = if (targetAccount.isDefined) {
      val acct = targetAccount.get
      new AWSCredentialsProvider() {
        override def refresh(): Unit = {}
        override def getCredentials: AWSCredentials = new BasicAWSCredentials(acct.awsAccessKey, acct.awsSecretAccessKey)
      }
    } else {
      new DefaultAWSCredentialsProviderChain()
    }

    val client = new AmazonKinesisClient(credentialsProvider)

    val region = targetAccount match {
      case Some(t) => Some(t.region)
      case None => Some("eu-west-1")
    }
    if (region.isDefined) {
      client.setRegion(Region.getRegion(Regions.fromName(region.get)))
    }

    client
  }

  def buildClient(targetAccount: Option[TargetAccount]) = {
    buildClientConfig(targetAccount)
  }

}
