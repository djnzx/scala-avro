import avrohugger.format.SpecificRecord
import avrohugger.types.OptionShapelessCoproduct
import sbt.Keys.*
import scala.collection.Seq

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val commonSettings = Seq(
  organization := "com.djnz",
  scalaVersion := "2.13.14",
  crossScalaVersions := Seq(Versions.scala213, Versions.scala212),
  javacOptions := Seq("-source", "11", "-target", "11"),
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:higherKinds",
    "-language:existentials",
    "-Wconf:cat=other-match-analysis:error",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Ywarn-dead-code",
    "-Yrepl-class-based"
  ),
  /** for io.confluent:kafka-avro-serializer:7.6.0 */
  resolvers += "confluent" at "https://packages.confluent.io/maven/",
  libraryDependencies ++= Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full),
    "org.typelevel"     %% "cats-core"            % "2.12.0",
    "org.typelevel"     %% "cats-effect"          % "3.5.4",
    "co.fs2"            %% "fs2-io"               % "3.10.2",
    "io.circe"          %% "circe-parser"         % "0.14.9",
    "io.circe"          %% "circe-generic-extras" % "0.14.4",
    "org.scalatest"     %% "scalatest"            % "3.2.19",
    "org.scalacheck"    %% "scalacheck"           % "1.18.0",
    "org.scalatestplus" %% "scalacheck-1-18"      % "3.2.19.0",
    "com.lihaoyi"       %% "pprint"               % "0.9.0",
    "org.apache.avro"    % "avro"                 % "1.11.3",
  ),
)

lazy val avro101 = (project in file("avro101"))
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)

lazy val vulcan101 = (project in file("vulcan101"))
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.avro"  % "avro"              % "1.11.3", // fasterxml.jackson
      "com.github.fd4s" %% "vulcan"            % "1.11.0", // apache avro + cats-free monad
      "com.github.fd4s" %% "vulcan-generic"    % "1.11.0", // vulcan + shapeless + magnolia
      "com.github.fd4s" %% "vulcan-enumeratum" % "1.11.0", // vulcan + vulcan-generic + enumeratum
    )
  )

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    name := "common"
  )

lazy val root = (project in file("."))
  .settings(
    name := "scala-avro-kafka-flows",
    version := "0.0.0"
  )
  .enablePlugins(NoPublishPlugin)
  .aggregate(
    avro101,
    vulcan101,
    topic1,
    topic2,
    topic3,
    topic4,
  )

lazy val topic1 = (project in file("topic1"))
  .enablePlugins(SbtAvro) // actually it's AutoPlugin, but we declare for clarity
  .settings(
    commonSettings,
    name := "avro-topic1",
    releaseVersionFile := baseDirectory.value / "topic1" / "version.sbt",
    Compile / avroSource := (Compile / sourceDirectory).value / "avro",
    Compile / doc := target.value / "none",
    /** included for clarify */
    Compile / sourceGenerators += (Compile / avroGenerate).taskValue,
    /** need to turn off since we have more than one plugin */
    Compile / sourceGenerators -= (Compile / avroScalaGenerateSpecific).taskValue,
  )

lazy val topic2 = (project in file("topic2"))
  .enablePlugins(SbtAvrohugger) // actually it's AutoPlugin, but we declare for clarity
  .settings(
    commonSettings,
    name := "avro-topic2",
    releaseVersionFile := baseDirectory.value / "topic2" / "version.sbt",
    autoScalaLibrary := true,
    Compile / avroScalaSpecificCustomTypes :=
      SpecificRecord.defaultTypes.copy(union = OptionShapelessCoproduct),
    /** need to turn off since we have more than one plugin */
    Compile / avroGenerate := Seq.empty,
    /** included for clarify */
    Compile / sourceGenerators += (Compile / avroScalaGenerateSpecific).taskValue,
  )

lazy val topic3 = (project in file("topic3"))
  .settings(
    name := "avro-topic3",
    releaseVersionFile := baseDirectory.value / "topic3" / "version.sbt",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.github.fd4s" %% "vulcan"            % "1.11.0",
      "com.github.fd4s" %% "vulcan-generic"    % "1.11.0",
      "com.github.fd4s" %% "vulcan-enumeratum" % "1.11.0",
      "com.github.fd4s" %% "fs2-kafka"         % "3.5.1",
      "com.github.fd4s" %% "fs2-kafka-vulcan"  % "3.5.1"
    ),
    Compile / doc := target.value / "none"
  )

lazy val topic4 = (project in file("topic4"))
  .settings(
    commonSettings,
    name := "avro-topic4",
    releaseVersionFile := baseDirectory.value / "topic4" / "version.sbt",
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core"  % "4.1.2",
      "com.sksamuel.avro4s" %% "avro4s-json"  % "4.1.2",
      "com.sksamuel.avro4s" %% "avro4s-kafka" % "4.1.2"
    ),
    /** need to turn off since we have more than one plugin */
    Compile / avroGenerate := Seq.empty,
  )
