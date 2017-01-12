
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "TrainStampRally"

scalaVersion := "2.11.8"

val play2AuthVer = "0.14.2"
val googleLibVer = "1.22.0"

libraryDependencies ++= Seq(
  ws,
  cache,
  "org.skinny-framework" %% "skinny-orm" % "2.2.0",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.0",
  "org.mariadb.jdbc" % "mariadb-java-client" % "1.4.0",
  "org.flywaydb" %% "flyway-play" % "3.0.1",
  "jp.t2v" %% "play2-auth" % play2AuthVer,
  "jp.t2v" %% "play2-auth-social" % play2AuthVer,
  "com.github.tototoshi" %% "play-json4s-native" % "0.5.0",
  "com.github.tototoshi" %% "play-json4s-test-native" % "0.5.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.springframework.security" % "spring-security-web" % "4.1.3.RELEASE",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "runtime",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "net.liftweb" %% "lift-util" % "2.6.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "com.google.maps" % "google-maps-services" % "0.1.17",
  "com.amazonaws" % "aws-java-sdk" % "1.11.75"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions += "-feature"

SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(SpacesAroundMultiImports, false)

excludeFilter in scalariformFormat := (excludeFilter in scalariformFormat).value ||
    "Routes.scala" ||
    "ReverseRoutes.scala" ||
    "JavaScriptReverseRoutes.scala" ||
    "RoutesPrefix.scala"

wartremoverWarnings ++= Warts.allBut(
  Wart.Any,
  Wart.NoNeedForMonad,
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.ToString,
  Wart.Nothing,
  Wart.Option2Iterable,
  Wart.ImplicitConversion,
  Wart.Equals,
  Wart.MutableDataStructures,
  Wart.NonUnitStatements
)

wartremoverExcluded ++= Seq(
  crossTarget.value / "routes" / "main" / "router" / "Routes.scala",
  crossTarget.value / "routes" / "main" / "router" / "RoutesPrefix.scala",
  crossTarget.value / "routes" / "main" / "controllers" / "ReverseRoutes.scala",
  crossTarget.value / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"
)
