package com.snowplowanalytics.kinesistee.config

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.{Failure, Success, Try}


class SelfDescribingData(json: String) {

  implicit val defaultFormats = org.json4s.DefaultFormats

  private val parsed = Try(parse(json)) match {
    case Failure(f) => throw new IllegalArgumentException(s"Invalid input JSON:\n\n$json", f)
    case Success(s) => s
  }

  val schema: String = (parsed \\ "schema").extractOrElse[String](throw new IllegalArgumentException("Invalid self describing schema: missing the `schema` field"))
  val data: String = pretty(parsed \\ "data")

}
