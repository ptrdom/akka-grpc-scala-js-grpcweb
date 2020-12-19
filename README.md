# Akka gRPC Scala.js gRPC-web

Built on:
- https://github.com/akka/akka-grpc
- https://github.com/scalapb/scalapb-grpcweb
- https://github.com/sbt/sbt-web
- https://github.com/vmunier/sbt-web-scalajs
- https://github.com/scalacenter/scalajs-bundler

## How to run

Using `sbt`:
- `"project server" ~compile`
- `"project server" ~reStart`
- Use `reStart` instead of `run`, otherwise `client` changes won't be detected.

Using `IntelliJ` (tested with `2020.3`):
- Use Run/Debug configurations provided in `/.run`.

## Issues 

1. `~reStart` triggers full `server` reload. Author of `sbt-web-scalajs` indicates that there's no workaround for that.
https://github.com/playframework/playframework with https://github.com/playframework/play-grpc might be a better 
backend because `Play`'s custom reload script shouldn't trigger on asset changes.
   
2. `client` bundling can be greatly sped up during development - https://scalacenter.github.io/scalajs-bundler/reference.html#bundling-mode-library-only.