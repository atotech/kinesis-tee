package com.snowplowanalytics.kinesistee.config

import awscala.dynamodbv2.DynamoDB

trait Builder {
  def build(tableName: String, functionName: String)(implicit dynamoDB: DynamoDB): Configuration
}
