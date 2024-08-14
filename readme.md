### avro / scala / vulcan / kafka - playground, API, compatibility

### avro101

avro fundamentals:
- schema
- reading
- writing

### vulcan101

Vulcan coded fundamentals

### topic1
- sbt plugin to generate `Java` classes extends `avro.SpecificRecord`
- for unions and nullable it uses `Object` as a type
- [GitHub link](https://github.com/sbt/sbt-avro)
- `plugins.sbt`:
    ```scala
    libraryDependencies += "org.apache.avro" % "avro-compiler" % "1.11.3"
    addSbtPlugin("com.github.sbt" % "sbt-avro" % "3.4.3")
    ```
- sbt task `avroGenerate`

### topic2
- sbt plugin to generate `Scala` case classes
  - can be plain case classes 
  - can be case classes extends `avro.SpecificRecord`
- for nullable uses `Option`
- for unions uses `Shapeless` `Coproduct`
- [GitHub link](https://github.com/julianpeeters/avrohugger)
- `plugins.sbt`:
    ```scala
    addSbtPlugin("com.julianpeeters" % "sbt-avrohugger" % "2.8.3")
    ```
- sbt task `avroScalaGenerate`         - plain case classes
- sbt task `avroScalaGenerateSpecific` - case classes extends `SpecificRecordBase`

### topic3
- library `vulcan` to generate schema from Scala case classes
  - Schemas, encoders, and decoders for many standard library types
  - Ability to easily create schemas, encoders, and decoders for custom types
  - Derivation of schemas, encoders, and decoders for case classes and sealed traits.
- inspired by `avro4s` but much cleaner
- [GitHub link](https://github.com/fd4s/vulcan)
- [Documentation](https://fd4s.github.io/vulcan/)
- [Documentation](https://fd4s.github.io/vulcan/docs/codecs)
- Avro doesn't support nested unions, must be nullable record with union as a field
- no plugins
- `build.sbt`
  ```scala
    libraryDependencies ++= Seq(
      "com.github.fd4s" %% "vulcan"            % "1.11.0",
      "com.github.fd4s" %% "vulcan-generic"    % "1.11.0",
      "com.github.fd4s" %% "vulcan-enumeratum" % "1.11.0",
      "com.github.fd4s" %% "fs2-kafka"         % "3.5.1",
      "com.github.fd4s" %% "fs2-kafka-vulcan"  % "3.5.1"
    )
  ```

### topic4
- library `avro4s` to generate schema from Scala case classes
- written by the `elastic4s` author
- [GitHub link](https://github.com/sksamuel/avro4s)
- Schema generation from classes at compile time
- Boilerplate free serialization of Scala types into Avro types
- Boilerplate free deserialization of Avro types to Scala types
- has different versioning for different Scala Versions
- scala 2.12/2.13 - 4.x
- scala 3.x       - 5.x
- it's based on json4s, has interop with Jackson & scalaz => so a bit legacy
- `build.sbt`
  ```scala
    libraryDependencies ++= Seq(
      "com.sksamuel.avro4s" %% "avro4s-core"  % "4.1.2",
      "com.sksamuel.avro4s" %% "avro4s-json"  % "4.1.2",
      "com.sksamuel.avro4s" %% "avro4s-kafka" % "4.1.2"
    )
  ```