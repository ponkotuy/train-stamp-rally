
name := "TrainStampRally"

scalaVersion := "2.11.8"

val play2AuthVer = "0.14.2"

libraryDependencies ++= Seq(
  cache,
  "org.skinny-framework" %% "skinny-orm" % "2.2.0",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.0",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.4.0",
  "org.flywaydb" %% "flyway-play" % "3.0.1",
  "jp.t2v" %% "play2-auth" % play2AuthVer,
  "jp.t2v" %% "play2-auth-social" % play2AuthVer,
  "com.github.tototoshi" %% "play-json4s-native" % "0.5.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.5.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
