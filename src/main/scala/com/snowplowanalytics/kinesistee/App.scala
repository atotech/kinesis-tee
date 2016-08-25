package com.snowplowanalytics.kinesistee

import java.lang.String

import awscala.dynamodbv2.DynamoDB
import com.amazonaws.regions.Region
import com.amazonaws.services.lambda.runtime.{Context => LambdaContext}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.snowplowanalytics.kinesistee.config.{Configuration, ConfigurationBuilder, Transformer}
import com.snowplowanalytics.kinesistee.filters.JavascriptFilter

import scala.collection.JavaConversions._
import scalaz._
import com.snowplowanalytics.kinesistee.models.{Content,Stream}
import com.snowplowanalytics.kinesistee.routing.PointToPointRoute
import com.snowplowanalytics.kinesistee.transformation.SnowplowToJson

object App {

  private val tee = KinesisTee

  /**
    * AWS Lambda entry point
    *
    * @param event an amazon kinesis event
    * @param context the context our lambda is in
    */
  def kinesisEventHandler(event: KinesisEvent, context: LambdaContext): Unit = {

     val conf = getConfiguration(context)

     val data = for { rec <- event.getRecords
                      row = new String(rec.getKinesis.getData.array(), "UTF-8") // unit test all this
                      content = Content(row)
                } yield content

    val sourceStream = Stream(conf.sourceStream.name)

    val transformation = conf.transformer match {
      case Transformer.SNOWPLOW_TO_JSON => Some(new SnowplowToJson)
      case _ => None
    }

    val filter = conf.filter match {
      case Some(f) => Some(new JavascriptFilter(f.javascript))
      case _ => None
    }

    val route = new PointToPointRoute(sourceStream, Stream(conf.targetStream.name)) // come back to me

    tee.tee(sourceStream,
            new StreamWriter(),
            transformation,
            filter,
            data)

  }

  def getConfiguration(context: LambdaContext): Configuration = {
    val region = LambdaUtils.getRegionFromArn(context.getInvokedFunctionArn) match {
      case Success(r) => r
      case Failure(f) => throw new IllegalStateException(f.toString())
    }

    val (confRegion, confTable) = LambdaUtils.getLambdaDescription(context.getFunctionName, region) match {
      case Success(desc) => {
        LambdaUtils.configLocationFromLambdaDesc(desc) match {
          case Success((region,table)) => (region, table)
          case Failure(f) => throw new IllegalStateException(f.toString())
        }
      }
      case Failure(f) => throw new IllegalStateException(f.toString())
    }

    scala.util.Try(ConfigurationBuilder.build(confTable, context.getFunctionName)(DynamoDB.at(Region.getRegion(confRegion)))) match {
      case scala.util.Success(c) => c
      case scala.util.Failure(f) => throw new IllegalStateException("Couldn't build configuration", f) // not much we can do here!
    }
  }



}
