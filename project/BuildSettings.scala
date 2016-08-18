/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import sbt.Keys._
import sbt._

object BuildSettings {

  lazy val basicSettings = Seq[Setting[_]](
    organization := "com.snowplowanalytics",
    version := "0.1.0",
    retrieveManaged := true,
    description := "Kinesis Tee",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature")
  )

  import sbtassembly.Plugin._
  import AssemblyKeys._

  lazy val sbtAssemblySettings = assemblySettings ++ Seq(
    jarName in assembly := {
      name.value + "-" + version.value + ".jar"
    },

    // META-INF discarding
    mergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

  lazy val buildSettings = basicSettings ++ sbtAssemblySettings
}
