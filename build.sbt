
name := "TrainStampRally"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  ws,
  ehcache,
  guice,
  "org.skinny-framework" %% "skinny-orm" % "2.3.5",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.6.0",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.4.0",
  "org.flywaydb" %% "flyway-play" % "4.0.0",
  "com.github.tototoshi" %% "play-json4s-native" % "0.8.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.8.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.springframework.security" % "spring-security-web" % "4.1.3.RELEASE",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "net.liftweb" %% "lift-util" % "3.1.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "com.google.maps" % "google-maps-services" % "0.1.17",
  "com.amazonaws" % "aws-java-sdk" % "1.11.75"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions += "-feature"

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

// Docker settings
dockerEntrypoint ++= Seq(
  "-Dconfig.resource=production.conf"
)
dockerRepository := Some("ponkotuy")
dockerUpdateLatest := true
