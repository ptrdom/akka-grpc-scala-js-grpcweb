package com.example.server

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import play.twirl.api.HtmlFormat

class WebService() extends Directives {

  private def scripts: HtmlFormat.Appendable =
    scalajs.html.scripts(
      "client",
      name => {
        val prefix = checksum(name)
          .map(_ + "-")
          .getOrElse("")
        s"/assets/$prefix$name"
      },
      name => {
        getClass.getResource(s"/public/$name") != null
      }
    )

  private def checksum(name: String) = {
    if (getClass.getResource(s"/public/$name.md5") != null) {
      Some(scala.io.Source.fromResource(s"public/$name.md5").getLines().mkString(""))
        .map(_.trim)
        .filter(_.nonEmpty)
    } else {
      None
    }
  }

  val route: Route = concat(
    pathSingleSlash {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            s"""
               |<!DOCTYPE html>
               |<html>
               |  <head>
               |    <title>Akka gRPC - Scala.js - gRPC Web</title>
               |  </head>
               |  <body>
               |    $scripts
               |  </body>
               |</html>
               |""".stripMargin
          )
        )
      }
    },
    pathPrefix("assets" / Remaining) { file =>
      // optionally compresses the response with Gzip or Deflate
      // if the client accepts compressed responses
      encodeResponse {
        getFromResource("public/" + file)
      }
    },
    path("favicon.ico") {
      complete("")
    }
  )
}
