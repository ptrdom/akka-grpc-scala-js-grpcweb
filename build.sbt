scalaVersion in ThisBuild := "2.13.2"

resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val akkaVersion     = "2.6.10"
lazy val akkaHttpVersion = "10.2.0"
lazy val akkaGrpcVersion = "1.0.2"
lazy val LogbackVersion  = "1.2.3"

lazy val `akka-grpc-scala-js-grpcweb` = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val proto =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("proto"))
    .enablePlugins(AkkaGrpcPlugin)
    .settings(
      PB.protoSources in Compile := Seq(
        (baseDirectory in ThisBuild).value / "proto" / "src" / "main" / "protobuf"
      )
    )
    .jsSettings(
      libraryDependencies += "com.thesamet.scalapb"         %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
      PB.targets in Compile := Seq(
        scalapb.gen(grpc = false)            -> (sourceManaged in Compile).value,
        scalapb.grpcweb.GrpcWebCodeGenerator -> (sourceManaged in Compile).value
      )
    )

lazy val protoJs  = proto.js
lazy val protoJVM = proto.jvm

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      // This is an application with a main method
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"
      //TODO setup https://scalacenter.github.io/scalajs-bundler/reference.html#bundling-mode-library-only
      //webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
      //webpackEmitSourceMaps in fastOptJS := false
    )
    .dependsOn(protoJs)

lazy val server = project
  .enablePlugins(AkkaGrpcPlugin, WebScalaJSBundlerPlugin)
  .in(file("server"))
  .settings(
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery"           % akkaVersion,
      "com.typesafe.akka" %% "akka-pki"                 % akkaVersion,
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http2-support"       % akkaHttpVersion,
      "ch.megard"         %% "akka-http-cors"           % "0.4.2",
      "com.vmunier"       %% "scalajs-scripts"          % "1.1.4",
      "ch.qos.logback"    % "logback-classic"           % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"      % akkaVersion % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.1" % Test
    )
  )
  .dependsOn(protoJVM)
