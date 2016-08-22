package com.snowplowanalytics.kinesistee.filters

import java.io.StringReader

import com.snowplowanalytics.kinesistee.models.{Content, Stream}
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.ScriptException

import scalaz.syntax.validation._
import scalaz.ValidationNel

class JavascriptFilter(js: String) extends FilterStrategy {

  // beware:
  // https://github.com/sbt/sbt/issues/1214
  val engine = new ScriptEngineManager(null).getEngineByName("nashorn")
  if (engine==null) { throw new IllegalStateException("Nashorn script engine not available") }
  val in: Invocable = engine.asInstanceOf[Invocable]

  engine.eval(new StringReader(js))

  override def filter(origin: Stream, content: Content): ValidationNel[Throwable, Boolean] = {
    try {
      val retVal = in.invokeFunction("filter", origin)
      retVal match {
        case bool:java.lang.Boolean => bool.booleanValue().success
        case e => new RuntimeException(s"'$e' returned by your js function cannot be converted to boolean").failureNel
      }
    } catch {
      case e @ (_: ScriptException | _: NoSuchMethodException ) => e.failureNel
    }
  }

}
