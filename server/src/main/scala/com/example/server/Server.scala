package com.example.server

import akka.actor.ActorSystem
import akka.grpc.scaladsl.WebHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import com.example.ServiceHandler
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object Server {
  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem("HelloWorld", conf)
    new Server(system).run()
  }
}

class Server(system: ActorSystem) extends Directives {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(): Future[Http.ServerBinding] = {
    implicit val actorSystem: ActorSystem = system
    import actorSystem.dispatcher

    val service: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      ServiceHandler.partial(new ServiceImpl())
    }

    val indexAndAssets = new WebService().route

    val grpcWebServiceHandlers = WebHandler.grpcWebHandler(service)

    val handlerRoute: Route = { ctx =>
      grpcWebServiceHandlers(ctx.request).map(RouteResult.Complete)
    }

    val route = concat(
      indexAndAssets,
      handlerRoute
    )

    val binding = Http()
      .newServerAt(
        interface = "localhost",
        port = 8080
      )
      .bind(Route.toFunction(route))

    binding.onComplete {
      case Success(binding) =>
        logger.info(s"gRPC server bound to: ${binding.localAddress}")

      case Failure(ex) =>
        logger.error(s"gRPC server binding failed", ex)
    }

    binding
  }
}
