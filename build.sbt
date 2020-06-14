name := "discussion-forum"
version := "1.0"
scalaVersion := "2.13.1"

libraryDependencies ++= {
  val akkaHttpV   = "10.1.12"
  val akkaV       = "2.6.5"
  val scalaTestV  = "3.1.2"
  val postgresV = "42.2.13"
  val slickV = "3.3.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "org.postgresql" % "postgresql" % postgresV,
    "com.typesafe.slick" %% "slick" % slickV
  )
}

