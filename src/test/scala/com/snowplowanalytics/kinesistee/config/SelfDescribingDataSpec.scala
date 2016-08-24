package com.snowplowanalytics.kinesistee.config

import org.specs2.mutable.Specification
import org.json4s._
import org.json4s.jackson.JsonMethods._

class SelfDescribingDataSpec extends Specification {

  "with valid self describing JSON" should {

    "give the right schema" in {
      val sdd = new SelfDescribingData(
        """
          |{
          |  "schema":"iglu:com.acme.banana",
          |  "data": { }
          |}
        """.stripMargin)

      sdd.schema mustEqual "iglu:com.acme.banana"
    }

    "give correctly formed json data payload" in {
      val sdd = new SelfDescribingData(
        """
          |{
          |  "schema":"iglu:com.acme.banana",
          |  "data": {
          |   "foo":"bar"
          |  }
          |}
        """.stripMargin)

      val expected = pretty(parse(
        """
          | { "foo":"bar" }
        """.stripMargin
      ))

      sdd.data mustEqual expected
    }


  }

  "with invalid self describing JSON" should {
    
  }
}
