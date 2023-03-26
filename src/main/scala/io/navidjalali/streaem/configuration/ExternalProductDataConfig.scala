package io.navidjalali.streaem.configuration

import zio.{ULayer, ZIO, ZLayer}

import scala.concurrent.duration.*

final case class ExternalProductDataConfig(
  scheme: String,
  host: String,
  port: Int,
  timeout: Duration
)

object ExternalProductDataConfig {
  def local: ULayer[ExternalProductDataConfig] =
    ZLayer.succeed(
      ExternalProductDataConfig(
        scheme = "http",
        host = "0.0.0.0",
        port = 4001,
        timeout = 5.seconds
      )
    )

  def envirnonment =
    ZLayer {
      for
        scheme <-
          ZIO
            .systemWith(_.env("MPD_SCHEME"))
            .orDie
            .flatMap(ZIO.fromOption(_))
        host <-
          ZIO
            .systemWith(_.env("MPD_HOST"))
            .orDie
            .flatMap(ZIO.fromOption(_))
        port <- ZIO
                  .systemWith(_.env("MPD_PORT"))
                  .orDie
                  .flatMap(ZIO.fromOption(_))
                  .map(_.toInt)
        timeout <- ZIO
                     .systemWith(_.env("MPD_TIMEOUT"))
                     .orDie
                     .flatMap(ZIO.fromOption(_))
                     .map(_.toInt.millis)
      yield ExternalProductDataConfig(
        scheme = scheme,
        host = host,
        port = port,
        timeout = timeout
      )
    }

  val live: ULayer[ExternalProductDataConfig] =
    envirnonment orElse local
}
