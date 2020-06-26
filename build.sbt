name := "discussion-forum"
version := "1.0"
scalaVersion := "2.13.1"
mainClass in Compile := Some("main.Main")

scalacOptions := Seq("-unchecked", "-deprecation")
enablePlugins(JavaAppPackaging)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= {
      val scalaTestV = "3.1.2"
      val akkaHttpV = "10.1.12"
      val akkaV = "2.6.5"
      val postgresV = "42.2.13"
      val slickV = "3.3.2"
      val scalaMockV = "4.4.0"
      Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaV,
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "com.typesafe.akka" %% "akka-testkit" % akkaV,
        "com.typesafe.akka" %% "akka-http" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
        "org.scalatest" %% "scalatest" % scalaTestV % "it,test",
        "org.scalamock" %% "scalamock" % scalaMockV % "test",
        "org.postgresql" % "postgresql" % postgresV,
        "com.typesafe.slick" %% "slick" % slickV,
        "org.scala-lang" % "scala-reflect" % "2.13.1",
        "com.typesafe.slick" %% "slick-testkit" % slickV % "test"
      )
    }

  )