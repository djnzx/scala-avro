addDependencyTreePlugin

/** https://github.com/ChristopherDavenport/sbt-no-publish - we will use for the root project */
addSbtPlugin("io.chrisdavenport" % "sbt-no-publish" % "0.1.0")

/** https://github.com/sbt/sbt-release */
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt"  % "2.5.2")
addSbtPlugin("com.eed3si9n"  % "sbt-buildinfo" % "0.12.0")

// #1. generate Java avro classes
libraryDependencies += "org.apache.avro" % "avro-compiler" % "1.11.3"
addSbtPlugin("com.github.sbt"            % "sbt-avro"      % "3.4.3")

// #2. generate Scala case classes
addSbtPlugin("com.julianpeeters" % "sbt-avrohugger" % "2.8.3")
