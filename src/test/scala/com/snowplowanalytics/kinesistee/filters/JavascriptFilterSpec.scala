package com.snowplowanalytics.kinesistee.filters

import com.snowplowanalytics.kinesistee.models.{Content, Stream}
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers

import scalaz.{Failure, Success}

class JavascriptFilterSpec extends Specification with ValidationMatchers {

  def buildStream = Stream("origin")

  "A valid JS filter" should {

   val jsTrue =
     """
       | function filter(origin) {
       |     return true;
       | }
     """.stripMargin

    val jsFalse =
      """
        | function filter(origin) {
        |     return false;
        | }
      """.stripMargin


    "with a js function that only returns true, return true" in {
      val strategy = new JavascriptFilter(jsTrue)
      strategy.filter(buildStream, Content("hello world")) must beSuccessful(true)
    }

    "with a js function that only returns false, return false" in {
      val strategy = new JavascriptFilter(jsFalse)
      strategy.filter(buildStream, Content("hello world")) must beSuccessful(false)
    }

  }

  "An invalid js filter" should {

    "fail if js is not well formed" in {
      val badlyFormedJs =
        """
          | function filter(origin) {
        """.stripMargin // no trailing slash

      val expectedError =
        """<eval>:3:8 Expected } but found eof
          |
          |        ^ in <eval> at line number 3 at column number 8""".stripMargin

      scala.util.Try(new JavascriptFilter(badlyFormedJs)) match {
        case scala.util.Success(_) => ko("Badly formed JS did not generate exception")
        case scala.util.Failure(f) => f.getMessage.replaceAll("\\s", "") mustEqual expectedError.replaceAll("\\s", "")
      }
    }

    "fail if the js is missing a 'filter' function" in {
      val missingfunc =
        """
          |function banana() {
          |   return false;
          |}
        """.stripMargin

      val strategy = new JavascriptFilter(missingfunc)
      strategy.filter(buildStream, Content("abc")) match {
        case Success(_) => ko("Filter cannot succeed without a 'filter' function")
        case Failure(f) => f.toString() mustEqual "NonEmptyList(java.lang.NoSuchMethodException: No such function filter)"
      }
    }

    "fail if the js has a runtime error" in {
      val runtimeBloop =
        """
          |function filter(org) { return 1/0; }
        """.stripMargin

      val strategy = new JavascriptFilter(runtimeBloop)
      strategy.filter(buildStream, Content("abc")) must beFailing
    }


  }
}
