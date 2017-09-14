import sbt._

object Dependencies {

  // basic
  lazy val dispatch         = "net.databinder.dispatch" %%  "dispatch-core"   % "0.13.1"
  lazy val `slf4j-simple`   = "org.slf4j"               %   "slf4j-simple"    % "1.7.25"

  // json libraries
  lazy val sprayJson        = "io.spray"                %%  "spray-json"      % "1.3.3"
  lazy val playJson         = "com.typesafe.play"       %   "play-json_2.12"  % "2.6.3"

  // testing
  lazy val scalaTest        = "org.scalatest"           %%  "scalatest"       % "3.0.4"
  lazy val wireMock         = "com.github.tomakehurst"  %   "wiremock"        % "2.8.0"

}
