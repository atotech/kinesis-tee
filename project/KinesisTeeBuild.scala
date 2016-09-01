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
// avroghugger
import sbtavrohugger.SbtAvrohugger._

object KinesisTeeBuild extends Build {

  import BuildSettings._
  import Dependencies._

  // Configure prompt to show current project.
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  // Define our project, with basic project information and library
  // dependencies.
  lazy val project = Project("kinesis-tee", file("."))
    .settings(buildSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        Libraries.scalaz7,
        Libraries.specs2,
        Libraries.scalazSpecs2,
        Libraries.json4s,
        Libraries.json4sExt,
        Libraries.analyticsSdk,
        Libraries.avro4s,
        Libraries.awsSdkCore,
        Libraries.awsSdk,
        Libraries.awsLambdaSdk,
        Libraries.awsLambda,
        Libraries.awsLambdaEvents,
        Libraries.awsKinesisSdk,
        Libraries.awsscala,
        Libraries.slf4jSimple
      )
    )

}
