package com.example.client

import com.example.service.Request
import com.example.service.Response
import com.example.service.ServiceGrpcWeb
import io.grpc.stub.StreamObserver
import org.scalajs.dom.document
import scalapb.grpc.Channels
import scalapb.grpcweb.Metadata

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.timers.setTimeout
import scala.util.Failure
import scala.util.Success

object Client {

  val stub = ServiceGrpcWeb.stub(Channels.grpcwebChannel("http://localhost:8080"))

  def main(args: Array[String]): Unit = {
    val helloWorldContainer = document.createElement("p")
    helloWorldContainer.textContent = "Hello world!"
    document.body.appendChild(helloWorldContainer)

    unary()
    stream(cancel = true)
    stream(cancel = false)
  }

  def unary() = {
    val container = document.createElement("div")
    container.textContent = "Unary request:"
    document.body.appendChild(container)

    val progress = document.createElement("p")
    progress.textContent = "Request pending"
    container.appendChild(progress)

    val req                = Request(payload = "Hello!")
    val metadata: Metadata = Metadata("custom-header-1" -> "unary-value")
    progress.textContent = "Request sent"

    stub.unary(req, metadata).onComplete {
      case Success(value) =>
        println("Success")
        progress.textContent = s"Request success: ${value.payload}"
      case Failure(ex) =>
        println("Failure")
        progress.textContent = s"Request failure: $ex"
    }
  }

  def stream(cancel: Boolean) = {
    val container = document.createElement("div")
    container.textContent = "Stream request:"
    document.body.appendChild(container)

    val progress = document.createElement("p")
    progress.textContent = "Request pending"
    container.appendChild(progress)

    val req                = Request(payload = "Hello!")
    val metadata: Metadata = Metadata("custom-header-2" -> "streaming-value")
    progress.textContent = "Request sent"

    var resCount = 0

    val stream = stub.serverStreaming(
      req,
      metadata,
      new StreamObserver[Response] {
        override def onNext(value: Response): Unit = {
          resCount += 1
          progress.textContent = s"Received success [$resCount]"
        }

        override def onError(ex: Throwable): Unit = {
          progress.textContent = s"Received failure: $ex"
        }

        override def onCompleted(): Unit = {
          progress.textContent = s"Received completed"
        }
      }
    )

    if (cancel) {
      setTimeout(5000) {
        progress.textContent = s"Stream stopped by client"
        stream.cancel()
      }
    }
  }
}
