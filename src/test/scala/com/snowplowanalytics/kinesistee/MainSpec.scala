package com.snowplowanalytics.kinesistee

import java.nio.ByteBuffer

import awscala.dynamodbv2.DynamoDB
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.snowplowanalytics.kinesistee.config.{TargetStream, Transformer, _}
import com.amazonaws.services.lambda.runtime.{Context => LambdaContext}
import com.snowplowanalytics.kinesistee.filters.FilterStrategy
import com.snowplowanalytics.kinesistee.models.{Content, Stream}
import com.snowplowanalytics.kinesistee.routing.{PointToPointRoute, RoutingStrategy}
import com.snowplowanalytics.kinesistee.transformation.TransformationStrategy
import org.mockito.Matchers.{eq => eqTo}

import scalaz.ValidationNel
import scalaz.syntax.validation._
import scala.collection.JavaConversions._
import scala.language.reflectiveCalls

class MainSpec extends Specification with Mockito {

  val sampleConfig = Configuration(name = "My Kinesis Tee example",
                                   sourceStream = SourceStream("my-source-stream", InitialPosition.TRIM_HORIZON, 10000),
                                   targetStream = TargetStream("my-target-stream", None),
                                   transformer = Transformer.SNOWPLOW_TO_JSON,
                                   filter = None)

  class MockMain extends Main {
    override val kinesisTee:Tee = mock[Tee]

    override val lambdaUtils:AwsLambdaUtils = {
      val util = mock[AwsLambdaUtils]
      util.getLambdaDescription(any[String], any[String]) returns "dynamodb:us-east-1/config".success
      util.getRegionFromArn(any[String]) returns "us-east-1".success
      util.configLocationFromLambdaDesc(any[String]) returns (Regions.US_EAST_1, "table-name").success
      util
    }

    override val configurationBuilder:Builder = {
      val builder = mock[Builder]
      builder.build(any[String], any[String])(any[DynamoDB]) returns sampleConfig
      builder
    }
  }

  val sampleArn = "arn:aws:elasticbeanstalk:us-east-1:123456789012:environment/My App/MyEnvironment"
  val sampleFunctionName = "fun"

  def sampleContext = {
    val context = mock[LambdaContext]
    context.getInvokedFunctionArn returns sampleArn
    context.getFunctionName returns sampleFunctionName
    context
  }

  def sampleKinesisEvent = {
    val event = mock[KinesisEvent]
    val record = mock[KinesisEventRecord]
    val kinesisRecord = mock[KinesisEvent.Record]

    kinesisRecord.getData returns ByteBuffer.wrap("hello world".getBytes("UTF-8"))
    record.getKinesis returns kinesisRecord

    event.getRecords returns List(record)
    event
  }

  "getting configuration" should {

      "use the lambda utils to grab the ARN" in {
        val main = new MockMain
        main.getConfiguration(sampleContext)
        there was one (main.lambdaUtils).getRegionFromArn(eqTo(sampleArn))
      }

     "throw an exception if the ARN cannot be ascertained" in {
       val main = new MockMain {
         override val lambdaUtils:AwsLambdaUtils = {
           val util = mock[AwsLambdaUtils]
           util.getRegionFromArn(any[String]) returns "Cannot handle it".failureNel
           util
         }
       }
       main.getConfiguration(sampleContext) must throwA[IllegalStateException](message = "Cannot handle it")
     }

    "use the given arn/function name to fetch lambda description" in {
      val mockMain = new MockMain
      mockMain.getConfiguration(sampleContext)
      there was one (mockMain.lambdaUtils).getLambdaDescription(eqTo(sampleFunctionName), eqTo("us-east-1"))
    }

    "throw an exception if the lambda description cannot be ascertained" in {
      val mockMain = new MockMain {
        override val lambdaUtils:AwsLambdaUtils = {
          val util = mock[AwsLambdaUtils]
          util.getLambdaDescription(any[String], any[String]) returns new RuntimeException("failed?").failureNel
          util.getRegionFromArn(any[String]) returns "us-east-1".success
        }
      }

      mockMain.getConfiguration(sampleContext) must throwA[IllegalStateException](message = "failed?")
    }

    "throw an exception if the lambda config location cannot be ascertained" in {
      val mockMain = new MockMain {
        override val lambdaUtils:AwsLambdaUtils = {
          val util = mock[AwsLambdaUtils]
          util.getLambdaDescription(any[String], any[String]) returns "dynamodb:sample/sample".success
          util.getRegionFromArn(any[String]) returns "us-east-1".success
          util.configLocationFromLambdaDesc(eqTo("dynamodb:sample/sample")) returns "oops".failureNel
        }
      }

      mockMain.getConfiguration(sampleContext) must throwA[IllegalStateException](message = "oops")
    }

    "use the lambda description to build config from" in {
      val main = new MockMain
      main.getConfiguration(sampleContext)
      there was one (main.configurationBuilder).build(eqTo("table-name"), eqTo(sampleFunctionName))(any[DynamoDB])
    }

    "throw an exception if the configuration fails to build" in {
      val main = new MockMain {
        override val configurationBuilder:Builder = {
          val cb = mock[Builder]
          cb.build(any[String], any[String])(any[DynamoDB]) throws new RuntimeException("broken")
        }
      }

      main.getConfiguration(sampleContext) must throwA[IllegalStateException](message="Couldn't build configuration")
    }

  }

  "the kinesis tee lambda entry point" should {

    "tee with the given records" in {
      val main = new MockMain
      main.kinesisEventHandler(sampleKinesisEvent, sampleContext)
      there was one (main.kinesisTee).tee(any[Stream],
                                          any[RoutingStrategy],
                                          any[Option[TransformationStrategy]],
                                          any[Option[FilterStrategy]],
                                          eqTo(Seq(Content("hello world"))))

    }

    "tee using the origin stream in the configuration" in {
      val main = new MockMain
      main.kinesisEventHandler(sampleKinesisEvent, sampleContext)
      there was one (main.kinesisTee).tee(eqTo(Stream(sampleConfig.sourceStream.name)),
                                          any[RoutingStrategy],
                                          any[Option[TransformationStrategy]],
                                          any[Option[FilterStrategy]],
                                          any[Seq[Content]])
    }

    "tee using the routing strategy point-to-point" in {
      val main = new MockMain {
        override val kinesisTee = new Tee {

          var lastRoutingStrategy: Option[PointToPointRoute] = None

          override def tee(source: Stream,
                           routingStrategy: RoutingStrategy,
                           transformationStrategy: Option[TransformationStrategy],
                           filterStrategy: Option[FilterStrategy],
                           content: Seq[Content]): Unit = {
            lastRoutingStrategy = Some(routingStrategy.asInstanceOf[PointToPointRoute])
          }
        }
      }
      main.kinesisEventHandler(sampleKinesisEvent, sampleContext)
      val expectedRouter = new PointToPointRoute(Stream(sampleConfig.sourceStream.name),
                                                 new StreamWriter(Stream(sampleConfig.targetStream.name),
                                                                  sampleConfig.targetStream.targetAccount))

      val lastRoutingStrategy:PointToPointRoute = main.kinesisTee.lastRoutingStrategy.get
      lastRoutingStrategy.toString mustEqual expectedRouter.toString
    }

  }

}
