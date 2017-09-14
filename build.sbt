import Dependencies._
import sbt.Keys.{libraryDependencies, parallelExecution}

lazy val commonSettings = Seq(
  organization := "com.alefeducation",
  scalaVersion := "2.12.1",
  version      := "0.1.0-SNAPSHOT",
  parallelExecution in Test := false,
  libraryDependencies += dispatch,
  libraryDependencies += `slf4j-simple`,
  libraryDependencies += scalaTest      % Test,
  libraryDependencies += wireMock       % Test
)

lazy val plainText = (project in file("PlainText"))
  .settings(commonSettings)

lazy val withPlayJson = (project in file("WithPlayJson"))
  .settings(commonSettings)

lazy val root = (project in file("."))
  .settings(
    inThisBuild(commonSettings),
    name := "wiremock-scala-examples"
  )
  .aggregate(withPlayJson)
