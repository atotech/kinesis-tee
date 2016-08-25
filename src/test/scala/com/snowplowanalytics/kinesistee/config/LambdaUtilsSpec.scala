package com.snowplowanalytics.kinesistee.config

import com.amazonaws.regions.Regions
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers

import scalaz.NonEmptyList

class LambdaUtilsSpec extends Specification with ValidationMatchers {

  "getting a region from an ARN" should {

    "get the correct region from a valid arn" in {
      val actual = LambdaUtils.getRegionFromArn("arn:aws:elasticbeanstalk:us-east-1:123456789012:environment/My App/MyEnvironment")
      actual must beSuccessful("us-east-1")
    }

    "an empty string must be rejected" in {
      val actual = LambdaUtils.getRegionFromArn(" ")
      actual must beFailing(NonEmptyList("Cannot extract region from an empty ARN"))
    }

    "an invalid string must be rejected with a message" in {
      val actual = LambdaUtils.getRegionFromArn("notvalid")
      actual must beFailing(NonEmptyList("Cannot extract region from ARN 'notvalid': invalid format"))
    }

  }

  "inflating a config from a URI" should {

    "inflate a valid URI" in {
      val uri = "dynamodb:eu-west-1/config-table"
      LambdaUtils.configLocationFromLambdaDesc(uri) must beSuccessful(Regions.fromName("eu-west-1"), "config-table")
    }

    "fail to inflate a valid URI with a strange resource type" in {
      val uri = "ddb:eu-west-1/config-table"
      LambdaUtils.configLocationFromLambdaDesc(uri) must beFailing(NonEmptyList("'ddb:eu-west-1/config-table' is not a valid configuration location - expected the format 'dynamodb:eu-west-1/config-table-name'"))
    }

    "fail to inflate a valid URI with an invalid region name" in {
      val uri = "dynamodb:notreal/config-table"
      LambdaUtils.configLocationFromLambdaDesc(uri) must beFailing(NonEmptyList("'notreal' is not a valid AWS region: Cannot create enum from notreal value!"))
    }

  }

}